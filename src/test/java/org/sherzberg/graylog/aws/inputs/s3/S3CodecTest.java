package org.sherzberg.graylog.aws.inputs.s3;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.journal.RawMessage;
import org.sherzberg.graylog.aws.json.S3Record;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class S3CodecTest {

    @Test
    public void testDecode() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        S3Record s3Record = new S3Record();
        s3Record.log = "this is a test message";
        s3Record.s3Bucket = "some-bucket";
        s3Record.s3ObjectKey = "mykey.tar.gz";

        RawMessage rawMessage = new RawMessage(objectMapper.writeValueAsBytes(s3Record));
        S3Codec s3Codec = new S3Codec(null, objectMapper);
        Message message = s3Codec.decode(rawMessage);

        assertTrue(message.getField("timestamp").toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}([\\+|\\-]\\d{2}:\\d{2}|Z)"));

        assertEquals(message.getField("source"), "s3");
        assertEquals(message.getField("message"), "this is a test message");
        assertEquals(message.getField("s3_bucket"), "some-bucket");
        assertEquals(message.getField("s3_object_key"), "mykey.tar.gz");
    }
}