package org.sherzberg.graylog.aws.inputs.s3.notifications;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class S3SQSClientTest {

    @Test
    public void testDeleteHappyPath() {
        AmazonSQSClient mock = mock(AmazonSQSClient.class);

        S3SQSClient s3SQSClient = new S3SQSClient(Region.getRegion(Regions.US_EAST_1), null, "", "");
        s3SQSClient.setAmazonSqs(mock);

        List<S3SNSNotification> notifications = new ArrayList<>();

        for (int i = 0; i < 11; i++) {
            notifications.add(new S3SNSNotification("asdf", "fdas", "1111"));
        }

        s3SQSClient.deleteNotifications(notifications);

        verify(mock, times(2)).deleteMessageBatch(any());
    }

}