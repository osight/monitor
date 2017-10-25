package com.osight.monitor.profiler.instrument.classloading;

import com.osight.monitor.profiler.plugin.PluginConfig;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface PluginClassInjector extends ClassInjector {
    PluginConfig getPluginConfig();
}
