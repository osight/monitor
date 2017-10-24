package com.osight.monitor.boot;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class DefaultAgentOption implements AgentOption {
    private final Instrumentation instrumentation;
    private final URL[] pluginJars;
    private final List<String> bootstrapJarPaths;

    public DefaultAgentOption(final Instrumentation instrumentation, final URL[] pluginJars, List<String> bootstrapJarPaths) {
        this.instrumentation = instrumentation;
        this.pluginJars = pluginJars;
        if (bootstrapJarPaths == null) {
            this.bootstrapJarPaths = Collections.emptyList();
        } else {
            this.bootstrapJarPaths = bootstrapJarPaths;
        }
    }

    @Override
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    @Override
    public URL[] getPluginJars() {
        return pluginJars;
    }

    @Override
    public List<String> getBootstrapJarPaths() {
        return bootstrapJarPaths;
    }
}
