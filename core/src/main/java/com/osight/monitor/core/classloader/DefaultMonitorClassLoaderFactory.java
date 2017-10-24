package com.osight.monitor.core.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class DefaultMonitorClassLoaderFactory implements IMonitorClassLoaderFactory {
    @Override
    public URLClassLoader createURLClassLoader(URL[] urls, ClassLoader parent) {
        return new MonitorURLClassLoader(urls, parent);
    }
}
