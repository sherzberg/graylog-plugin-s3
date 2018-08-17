package org.sherzberg.graylog.aws.inputs.s3;

import com.google.common.eventbus.EventBus;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.lifecycles.Lifecycle;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class S3TransportTest {

    private S3Transport s3Transport;

    @BeforeTest
    public void setup() {
        Configuration mock = mock(Configuration.class);
        ServerStatus mock1 = mock(ServerStatus.class);
        EventBus mock2 = mock(EventBus.class);
        LocalMetricRegistry mock3 = mock(LocalMetricRegistry.class);

        this.s3Transport = new S3Transport(mock, mock2, mock1, mock3);
    }

    @DataProvider(name = "pauseEvents")
    public static Object[][] pauseEvents() {
        return new Object[][]{{Lifecycle.PAUSED}, {Lifecycle.FAILED}, {Lifecycle.THROTTLED}, {Lifecycle.HALTING}};
    }

    @Test(dataProvider = "pauseEvents")
    public void testHandleLifecyclePauseEvents(Lifecycle lifecycle) {
        S3Subscriber mockSubscriber = mock(S3Subscriber.class);
        s3Transport.setSubscriber(mockSubscriber);

        s3Transport.lifecycleStateChange(lifecycle);

        verify(mockSubscriber, times(1)).pause();
        verify(mockSubscriber, never()).unpause();
    }

}