package com.osight.monitor.boot;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface AgentOption {
    Instrumentation getInstrumentation();

    URL[] getPluginJars();

    List<String> getBootstrapJarPaths();
}
