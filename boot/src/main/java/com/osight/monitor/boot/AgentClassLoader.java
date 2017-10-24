package com.osight.monitor.boot;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;

import com.osight.monitor.core.Agent;
import com.osight.monitor.core.classloader.MonitorClassLoaderFactory;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class AgentClassLoader {
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    private final URLClassLoader classLoader;

    private String bootClass;

    private final ContextClassLoaderExecuteTemplate<Object> executeTemplate;

    AgentClassLoader(URL[] urls) {
        if (urls == null) {
            throw new NullPointerException("urls");
        }

        ClassLoader bootStrapClassLoader = AgentClassLoader.class.getClassLoader();
        this.classLoader = createClassLoader(urls, bootStrapClassLoader);

        this.executeTemplate = new ContextClassLoaderExecuteTemplate<Object>(classLoader);
    }


    public Agent boot(final AgentOption agentOption) {

        final Class<?> bootStrapClazz = getBootStrapClass();

        final Object agent = executeTemplate.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    Constructor<?> constructor = bootStrapClazz.getConstructor(AgentOption.class);
                    return constructor.newInstance(agentOption);
                } catch (InstantiationException e) {
                    throw new RuntimeException("boot create failed. Error:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("boot method invoke failed. Error:" + e.getMessage(), e);
                }
            }
        });

        if (agent instanceof Agent) {
            return (Agent) agent;
        } else {
            String agentClassName;
            if (agent == null) {
                agentClassName = "Agent is null";
            } else {
                agentClassName = agent.getClass().getName();
            }
            throw new RuntimeException("Invalid AgentType. boot failed. AgentClass:" + agentClassName);
        }
    }


    private URLClassLoader createClassLoader(final URL[] urls, final ClassLoader bootStrapClassLoader) {
        if (SECURITY_MANAGER != null) {
            return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                public URLClassLoader run() {
                    return MonitorClassLoaderFactory.createClassLoader(urls, bootStrapClassLoader);
                }
            });
        } else {
            return MonitorClassLoaderFactory.createClassLoader(urls, bootStrapClassLoader);
        }
    }


    private Class<?> getBootStrapClass() {
        try {
            return this.classLoader.loadClass(bootClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("boot class not found. bootClass:" + bootClass + " Error:" + e.getMessage(), e);
        }
    }

    void setBootClass(String bootClass) {
        this.bootClass = bootClass;
    }

}
