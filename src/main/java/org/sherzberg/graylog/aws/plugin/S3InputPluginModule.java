package org.sherzberg.graylog.aws.plugin;

import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;
import org.sherzberg.graylog.aws.inputs.s3.S3Codec;
import org.sherzberg.graylog.aws.inputs.s3.S3Input;
import org.sherzberg.graylog.aws.inputs.s3.S3Transport;

import java.util.Collections;
import java.util.Set;

/**
 * Extend the PluginModule abstract class here to add you plugin to the system.
 */
public class S3InputPluginModule extends PluginModule {
    /**
     * Returns all configuration beans required by this plugin.
     * <p>
     * Implementing this method is optional. The default method returns an empty {@link Set}.
     */
    @Override
    public Set<? extends PluginConfigBean> getConfigBeans() {
        return Collections.emptySet();
    }

    @Override
    protected void configure() {
        addCodec(S3Codec.NAME, S3Codec.class);
        addTransport(S3Transport.NAME, S3Transport.class);
        addMessageInput(S3Input.class);
    }
}
