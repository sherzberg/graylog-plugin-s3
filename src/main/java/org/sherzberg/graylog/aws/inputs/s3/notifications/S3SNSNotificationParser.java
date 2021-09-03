package org.sherzberg.graylog.aws.inputs.s3.notifications;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.sherzberg.graylog.aws.json.SQSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class S3SNSNotificationParser {
    private static final Logger LOG = LoggerFactory.getLogger(S3SNSNotificationParser.class);
    private final ObjectMapper om;

    public S3SNSNotificationParser() {
        om = new ObjectMapper();
    }

    public List<S3SNSNotification> parse(Message message) {
        List<S3SNSNotification> notifications = Lists.newArrayList();
        String messageBody = message.getBody();

        try {
            SQSMessage envelope = om.readValue(messageBody, SQSMessage.class);

            if (envelope.message == null) {
                return Collections.emptyList();
            }

            S3EventNotification s3EventNotification = S3EventNotification.parseJson(envelope.message);
            List<S3EventNotification.S3EventNotificationRecord> records = s3EventNotification.getRecords();

            if (records == null) {
                if (!envelope.message.contains("s3:TestEvent")){
                    LOG.warn("Consumed SNS notification does not have any record: {}", envelope.message);
                }
                return Collections.emptyList();
            }

            notifications.addAll(records.stream().map(record -> new S3SNSNotification(
                    message.getReceiptHandle(),
                    record.getS3().getBucket().getName(),
                    record.getS3().getObject().getUrlDecodedKey()
            )).collect(Collectors.toList()));
        } catch (Exception e) {
            LOG.error("Could not parse SNS notification: " + messageBody, e);
            throw new RuntimeException("Could not parse SNS notification: " + messageBody, e);
        }

        return notifications;
    }

}
