package org.sherzberg.graylog.aws.inputs.s3;


import org.graylog2.plugin.Message;
import org.graylog2.plugin.journal.RawMessage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class S3CodecTest {

    @Test
    public void testDecode() throws Exception {
        byte[] messageBytes = ("this is a test message").getBytes();
        RawMessage rawMessage = new RawMessage(messageBytes);
        S3Codec s3Codec = new S3Codec(null);
        Message message = s3Codec.decode(rawMessage);

        assertTrue(message.getField("timestamp").toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}([\\+|\\-]\\d{2}:\\d{2}|Z)"));

        assertEquals(message.getField("source"), "s3");
        assertEquals(message.getField("message"), "this is a test message");
    }
}