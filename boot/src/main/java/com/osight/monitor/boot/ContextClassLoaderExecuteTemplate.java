package com.osight.monitor.boot;

import java.util.concurrent.Callable;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class ContextClassLoaderExecuteTemplate<V> {
    private final ClassLoader classLoader;

    ContextClassLoaderExecuteTemplate(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public V execute(Callable<V> callable) {
        try {
            final Thread currentThread = Thread.currentThread();
            final ClassLoader before = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(ContextClassLoaderExecuteTemplate.this.classLoader);
            try {
                return callable.call();
            } finally {
                currentThread.setContextClassLoader(before);
            }
        } catch (Exception ex) {
            throw new RuntimeException("execute fail. Error:" + ex.getMessage(), ex);
        }
    }
}
