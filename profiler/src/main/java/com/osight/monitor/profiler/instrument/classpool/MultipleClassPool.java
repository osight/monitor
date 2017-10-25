package com.osight.monitor.profiler.instrument.classpool;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface MultipleClassPool {
    NamedClassPool getClassPool(ClassLoader classLoader);
}
