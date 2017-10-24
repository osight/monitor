package com.osight.monitor.boot;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;

import com.osight.monitor.core.Agent;


/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class MonitorStarter {
    private final BootLogger logger = BootLogger.getLogger(MonitorStarter.class.getName());

    public static final String BOOT_CLASS = "com.osight.monitor.profiler.DefaultAgent";

    private final BootstrapJarFile bootstrapJarFile;
    private final ClassPathResolver classPathResolver;
    private final Instrumentation instrumentation;

    MonitorStarter(BootstrapJarFile bootstrapJarFile, ClassPathResolver classPathResolver, Instrumentation instrumentation) {
        this.bootstrapJarFile = bootstrapJarFile;
        this.classPathResolver = classPathResolver;
        this.instrumentation = instrumentation;
    }

    boolean start() {
        URL[] pluginJars = classPathResolver.resolvePlugins();

        List<URL> libUrlList = resolveLib(classPathResolver);
        AgentClassLoader agentClassLoader = new AgentClassLoader(libUrlList.toArray(new URL[libUrlList.size()]));
        agentClassLoader.setBootClass(BOOT_CLASS);

        AgentOption option = createAgentOption(instrumentation, pluginJars, bootstrapJarFile);
        Agent monitorAgent = agentClassLoader.boot(option);
        monitorAgent.start();
        return true;
    }


    private AgentOption createAgentOption(Instrumentation instrumentation, URL[] pluginJars, BootstrapJarFile bootstrapJarFile) {
        List<String> bootstrapJarPaths = bootstrapJarFile.getJarNameList();
        return new DefaultAgentOption(instrumentation, pluginJars, bootstrapJarPaths);
    }


    private List<URL> resolveLib(ClassPathResolver classPathResolver) {
        // this method may handle only absolute path,  need to handle relative path (./..agentlib/lib)
        return classPathResolver.resolveLib();
    }

}
