package org.sherzberg.graylog.aws.config;

import com.fasterxml.jackson.annotation.*;
import com.google.auto.value.AutoValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@AutoValue
public abstract class AWSPluginConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(AWSPluginConfiguration.class);

    @JsonProperty("access_key")
    public abstract String accessKey();

    @JsonProperty("secret_key")
    public abstract String secretKey();

    @JsonCreator
    public static AWSPluginConfiguration create(@JsonProperty("lookups_enabled") boolean lookupsEnabled,
                                                @JsonProperty("lookup_regions") String lookupRegions,
                                                @JsonProperty("access_key") String accessKey,
                                                @JsonProperty("secret_key") String secretKey) {
        return builder()
                .accessKey(accessKey)
                .secretKey(secretKey)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AWSPluginConfiguration.Builder();
    }

    @JsonIgnore
    public boolean isComplete() {
        return accessKey() != null && secretKey() != null
                && !accessKey().isEmpty() && !secretKey().isEmpty();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder {

        public abstract Builder accessKey(String accessKey);

        public abstract Builder secretKey(String secretKey);

        public abstract AWSPluginConfiguration build();
    }

}
