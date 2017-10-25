package com.osight.monitor.profiler.plugin;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface ClassNameFilter {
    boolean accept(String className);
}
