package org.sherzberg.graylog.aws.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class S3Record {

    @JsonProperty("s3Bucket")
    public String s3Bucket;

    @JsonProperty("s3ObjectKey")
    public String s3ObjectKey;

    @JsonProperty("log")
    public String log;

    public Map<String, Object> getFields() {
        HashedMap fields = new HashedMap();
        fields.put("s3_bucket", s3Bucket);
        fields.put("s3_object_key", s3ObjectKey);
        return fields;
    }

}
