package com.osight.monitor.profiler.plugin;

import com.osight.monitor.core.instrument.InstrumentContext;
import com.osight.monitor.profiler.instrument.InstrumentEngine;
import com.osight.monitor.profiler.instrument.classloading.ClassInjector;
import com.osight.monitor.profiler.instrument.classloading.PluginClassInjector;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class PluginInstrumentContext implements InstrumentContext {
    private final ClassInjector classInjector;
    private final InstrumentEngine instrumentEngine;

    public PluginInstrumentContext(InstrumentEngine instrumentEngine,ClassInjector classInjector){
        this.instrumentEngine = instrumentEngine;
        this.classInjector = classInjector;

    }
    public PluginConfig getPluginConfig() {
        if (classInjector instanceof PluginClassInjector) {
            return ((PluginClassInjector) classInjector).getPluginConfig();
        }
        return null;
    }
    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        return null;
    }
}
