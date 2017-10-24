package com.osight.monitor.core.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class MonitorURLClassLoader extends URLClassLoader {
    private final ClassLoader parent;


    MonitorURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.parent = parent;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = findLoadedClass(name);
        if (clazz == null) {
            try {
                clazz = parent.loadClass(name);
            } catch (ClassNotFoundException ignore) {
            }
            if (clazz == null) {
                clazz = findClass(name);
            }
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }
}
