package com.osight.monitor.profiler.plugin;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class DefaultPluginContextLoadResult implements PluginContextLoadResult {
    @Override
    public List<ClassFileTransformer> getClassFileTransformer() {
        return null;
    }
}
