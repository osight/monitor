package com.osight.monitor.profiler.plugin;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface PluginContextLoadResult {
    List<ClassFileTransformer> getClassFileTransformer();
}
