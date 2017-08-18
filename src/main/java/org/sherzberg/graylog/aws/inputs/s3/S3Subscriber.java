package org.sherzberg.graylog.aws.inputs.s3;

import com.amazonaws.regions.Region;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Uninterruptibles;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.journal.RawMessage;
import org.sherzberg.graylog.aws.inputs.s3.notifications.S3SNSNotification;
import org.sherzberg.graylog.aws.inputs.s3.notifications.S3SQSClient;
import org.sherzberg.graylog.aws.json.S3Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class S3Subscriber extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(S3Subscriber.class);

    public static final int SLEEP_INTERVAL_SECS = 5;
    private final int threadCount;
    private volatile boolean stopped = false;

    private volatile boolean paused = false;
    private volatile CountDownLatch pausedLatch = new CountDownLatch(0);
    private final MessageInput sourceInput;
    private final ScheduledThreadPoolExecutor executor;

    private final S3SQSClient subscriber;
    private final S3Reader s3Reader;

    public S3Subscriber(Region sqsRegion, Region s3Region, String queueName, MessageInput sourceInput, String accessKey, String secretKey, int threadCount) {
        this.sourceInput = sourceInput;

        this.subscriber = new S3SQSClient(
                sqsRegion,
                queueName,
                accessKey,
                secretKey
        );

        this.s3Reader = new S3Reader(s3Region, accessKey, secretKey);
        this.threadCount = threadCount;
        this.executor = new ScheduledThreadPoolExecutor(threadCount, Executors.defaultThreadFactory());
    }

    public void pause() {
        paused = true;
        pausedLatch = new CountDownLatch(1);
    }

    // "resume" is already defined in the super class...
    public void unpause() {
        paused = false;
        pausedLatch.countDown();
    }

    private class Processor implements Runnable {
        @Override
        public void run() {
            final ObjectMapper objectMapper = new ObjectMapper();

            while (!stopped) {
                while (!stopped) {
                    if (paused) {
                        LOG.debug("Processing paused");
                        Uninterruptibles.awaitUninterruptibly(pausedLatch);
                    }
                    if (stopped) {
                        break;
                    }

                    List<S3SNSNotification> notifications;
                    try {
                        notifications = subscriber.getNotifications();
                    } catch (Exception e) {
                        LOG.error("Could not read messages from SNS. This is most likely a misconfiguration of the plugin. Going into sleep loop and retrying.", e);
                        break;
                    }

                    /*
                     * Break out and wait a few seconds until next attempt to avoid hammering AWS with SQS
                     * read requests while still being able to read lots of queued notifications without
                     * the sleep() between each.
                     */
                    if (notifications.size() == 0) {
                        LOG.debug("No more messages to read from SQS. Going into sleep loop.");
                        break;
                    }

                    LOG.debug("Processing " + notifications.size() + " S3 notifications in SQS");
                    for (S3SNSNotification n : notifications) {
                        try {
                            LOG.info("Reading messages from S3 file " + n.getS3Bucket() + "/" + n.getS3ObjectKey());

                            InputStream stream;

                            // TODO: do this better
                            if (n.getS3ObjectKey().endsWith("gz")) {
                                stream = s3Reader.readCompressedStream(n.getS3Bucket(), n.getS3ObjectKey());
                            } else {
                                stream = s3Reader.readStream(n.getS3Bucket(), n.getS3ObjectKey());
                            }

                            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                            String message;
                            while ((message = reader.readLine()) != null) {
                                S3Record s3Record = new S3Record();
                                s3Record.s3Bucket = n.getS3Bucket();
                                s3Record.s3ObjectKey = n.getS3ObjectKey();
                                s3Record.log = new String(message.getBytes(), StandardCharsets.UTF_8);

                                RawMessage rawMessage = new RawMessage(objectMapper.writeValueAsBytes(s3Record));

                                sourceInput.processRawMessage(rawMessage);
                            }

                            stream.close();

                        } catch (Exception e) {
                            LOG.error("Could not read s3 log file for <{}>. Skipping.", n.getS3Bucket() + "/" + n.getS3ObjectKey(), e);
                        }
                    }
                    subscriber.deleteNotifications(notifications);
                }

                if (!stopped) {
                    LOG.debug("Waiting {} seconds until next S3 SQS check.", SLEEP_INTERVAL_SECS);
                    Uninterruptibles.sleepUninterruptibly(SLEEP_INTERVAL_SECS, TimeUnit.SECONDS);
                }
            }
            LOG.debug("Thread exiting");
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < threadCount; i++) {
            executor.execute(new Processor());
        }
    }

    public void terminate() {
        stopped = true;
        paused = false;
        pausedLatch.countDown();
        try {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}