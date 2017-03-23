package org.sherzberg.graylog.aws.inputs.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class S3Reader {

    private final AmazonS3 s3Client;

    public S3Reader(Region region, String accessKey, String secretKey) {
        AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard();
        clientBuilder.setRegion(region.getName());

        if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
            clientBuilder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
        }

        this.s3Client = clientBuilder.build();
    }

    public InputStream readStream(String bucket, String key) throws IOException {
        S3Object o = getS3Object(bucket, key);

        return o.getObjectContent();
    }

    public InputStream readCompressedStream(String bucket, String key) throws IOException {
        S3Object o = getS3Object(bucket, key);

        return new GZIPInputStream(o.getObjectContent());
    }

    private S3Object getS3Object(String bucket, String key) {
        S3Object o = s3Client.getObject(bucket, key);

        if (o == null) {
            throw new RuntimeException("Could not get S3 object from bucket [" + bucket + "].");
        }
        return o;
    }

}