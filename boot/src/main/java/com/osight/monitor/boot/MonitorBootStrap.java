package com.osight.monitor.boot;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class MonitorBootStrap {
    private static final BootLogger logger = BootLogger.getLogger(MonitorBootStrap.class.getName());
    private static final LoadState STATE = new LoadState();

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        final boolean success = STATE.start();
        if (!success) {
            logger.warn("monitor already started. skipping agent loading.");
            return;
        }

        ClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver();
        if (!classPathResolver.verify()) {
            logger.warn("Agent Directory Verify fail. skipping agent loading.");
            return;
        }

        BootstrapJarFile bootstrapJarFile = classPathResolver.getBootstrapJarFile();
        appendToBootstrapClassLoader(instrumentation, bootstrapJarFile);
    }


    private static void appendToBootstrapClassLoader(Instrumentation instrumentation, BootstrapJarFile agentJarFile) {
        List<JarFile> jarFileList = agentJarFile.getJarFileList();
        for (JarFile jarFile : jarFileList) {
            logger.info("appendToBootstrapClassLoader:" + jarFile.getName());
            instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
        }
    }
}
