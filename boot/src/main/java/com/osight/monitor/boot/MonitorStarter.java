package com.osight.monitor.boot;

import java.lang.instrument.Instrumentation;
import java.net.URL;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class MonitorStarter {
    private final BootLogger logger = BootLogger.getLogger(MonitorStarter.class.getName());

    private final BootstrapJarFile bootstrapJarFile;
    private final ClassPathResolver classPathResolver;
    private final Instrumentation instrumentation;

    MonitorStarter(BootstrapJarFile bootstrapJarFile, ClassPathResolver classPathResolver, Instrumentation instrumentation){
        this.bootstrapJarFile = bootstrapJarFile;
        this.classPathResolver = classPathResolver;
        this.instrumentation = instrumentation;
    }
    boolean start(){
        URL[] pluginJars = classPathResolver.resolvePlugins();

        return true;
    }
}
