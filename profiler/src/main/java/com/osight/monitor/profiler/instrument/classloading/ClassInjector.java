package com.osight.monitor.profiler.instrument.classloading;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface ClassInjector {
    <T> Class<? extends T> injectClass(ClassLoader classLoader, String className);
}
