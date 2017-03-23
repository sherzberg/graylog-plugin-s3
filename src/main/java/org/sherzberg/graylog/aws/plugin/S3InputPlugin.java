package org.sherzberg.graylog.aws.plugin;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class S3InputPlugin implements Plugin {

    @Override
    public Collection<PluginModule> modules() {
        return Collections.singleton(new S3InputPluginModule());
    }

    @Override
    public PluginMetaData metadata() {
        return new S3InputPluginMetaData();
    }

}
