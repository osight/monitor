package com.osight.monitor.core.classloader;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class MonitorClassLoaderFactory {
    private static final IMonitorClassLoaderFactory CLASS_LOADER_FACTORY = createClassLoaderFactory();
    private static final String PARALLEL_CAPABLE_CLASS_LOADER_FACTORY = "com.osight.monitor.core.classloader.ParallelCapablePinpointClassLoaderFactory";

    public static URLClassLoader createClassLoader(URL[] urls, ClassLoader parent) {
        return CLASS_LOADER_FACTORY.createURLClassLoader(urls, parent);
    }

    private static IMonitorClassLoaderFactory createClassLoaderFactory() {
        boolean hasRegisterAsParallelCapableMethod = hasRegisterAsParallelCapableMethod();
        if (hasRegisterAsParallelCapableMethod) {
            try {
                ClassLoader classLoader = getClassLoader(MonitorClassLoaderFactory.class.getClassLoader());
                final Class<? extends IMonitorClassLoaderFactory> parallelCapableClassLoaderFactoryClass =
                        (Class<? extends IMonitorClassLoaderFactory>) Class.forName(PARALLEL_CAPABLE_CLASS_LOADER_FACTORY, true, classLoader);
                return parallelCapableClassLoaderFactoryClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new DefaultMonitorClassLoaderFactory();
        } else {
            return new DefaultMonitorClassLoaderFactory();
        }

    }

    private static boolean hasRegisterAsParallelCapableMethod() {
        Method[] methods = ClassLoader.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("registerAsParallelCapable")) {
                return true;
            }
        }

        return false;
    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }
}
