package org.sherzberg.graylog.aws.plugin;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class S3InputPluginMetaData implements PluginMetaData {
    private static final String PLUGIN_PROPERTIES = "org.sherzberg.graylog.plugins.graylog-plugin-s3/graylog-plugin.properties";

    @Override
    public String getUniqueId() {
        return "org.sherzberg.graylog.aws.plugin.S3InputPlugin";
    }

    @Override
    public String getName() {
        return "S3InputPlugin";
    }

    @Override
    public String getAuthor() {
        return "Spencer Herzberg <spencer.herzberg@gmail.com>";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/https://github.com/sherzberg/graylog-plugin-s3");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public String getDescription() {
        return "Pulls files from s3";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
