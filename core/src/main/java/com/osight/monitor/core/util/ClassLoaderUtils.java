package com.osight.monitor.core.util;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class ClassLoaderUtils {
    public static final ClassLoaderCallable DEFAULT_CLASS_LOADER_CALLABLE = new ClassLoaderCallable() {
        @Override
        public ClassLoader getClassLoader() {
            return ClassLoaderUtils.class.getClassLoader();
        }
    };

    private static final ClassLoader SYSTEM_CLASS_LOADER;
    private static final ClassLoader EXT_CLASS_LOADER;
    private static final ClassLoader BOOT_CLASS_LOADER;

    static {
        SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader();
        if (SYSTEM_CLASS_LOADER != null) {
            EXT_CLASS_LOADER = SYSTEM_CLASS_LOADER.getParent();
        } else {
            EXT_CLASS_LOADER = null;
        }
        if (EXT_CLASS_LOADER != null) {
            BOOT_CLASS_LOADER = EXT_CLASS_LOADER.getParent();
        } else {
            BOOT_CLASS_LOADER = null;
        }
    }

    private ClassLoaderUtils() {
    }

    public static ClassLoader getDefaultClassLoader() {
        return getDefaultClassLoader(DEFAULT_CLASS_LOADER_CALLABLE);
    }

    public static ClassLoader getDefaultClassLoader(ClassLoaderCallable defaultClassLoaderCallable) {
        if (defaultClassLoaderCallable == null) {
            throw new NullPointerException("defaultClassLoaderCallable must not be null");
        }

        try {
            final Thread th = Thread.currentThread();
            final ClassLoader contextClassLoader = th.getContextClassLoader();
            if (contextClassLoader != null) {
                return contextClassLoader;
            }
        } catch (Throwable ignore) {
        }
        return defaultClassLoaderCallable.getClassLoader();
    }

    public interface ClassLoaderCallable {
        ClassLoader getClassLoader();
    }


    public static boolean isJvmClassLoader(ClassLoader classLoader) {
        if (BOOT_CLASS_LOADER == classLoader || SYSTEM_CLASS_LOADER == classLoader || EXT_CLASS_LOADER == classLoader) {
            return true;
        }
        return false;
    }
}
