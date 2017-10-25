package com.osight.monitor.profiler.instrument;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osight.monitor.core.instrument.InstrumentClass;
import com.osight.monitor.core.instrument.InstrumentContext;
import com.osight.monitor.profiler.instrument.classpool.IsolateMultipleClassPool;
import com.osight.monitor.profiler.instrument.classpool.MultipleClassPool;
import com.osight.monitor.profiler.instrument.classpool.NamedClassPool;
import com.osight.monitor.profiler.plugin.PluginConfig;
import com.osight.monitor.profiler.plugin.PluginInstrumentContext;
import com.osight.monitor.profiler.util.JavaAssistUtils;

import javassist.ByteArrayClassPath;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class JavassistEngine implements InstrumentEngine {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Instrumentation instrumentation;
    private final MultipleClassPool childClassPool;

    public JavassistEngine(Instrumentation instrumentation, final List<String> bootStrapJars) {
        this.instrumentation = instrumentation;

        this.childClassPool = new IsolateMultipleClassPool(new IsolateMultipleClassPool.ClassPoolHandler() {
            @Override
            public void handleClassPool(NamedClassPool systemClassPool) {
                try {
                    if (bootStrapJars != null) {
                        for (String bootStrapJar : bootStrapJars) {
                            systemClassPool.appendClassPath(bootStrapJar);
                        }
                    }
                } catch (NotFoundException ex) {
                    throw new RuntimeException("bootStrapJar not found. Caused by:" + ex.getMessage(), ex);
                }
                systemClassPool.appendClassPath(new ClassClassPath(this.getClass()));
            }
        });
    }

    @Override
    public InstrumentClass getClass(InstrumentContext instrumentContext, ClassLoader classLoader, String classInternalName, byte[] classFileBuffer) {
        final CtClass cc = getCtClass(instrumentContext, classLoader, classInternalName, classFileBuffer);
        return new JavassistClass(instrumentContext, classLoader, cc);
    }

    @Override
    public boolean hasClass(ClassLoader classLoader, String classBinaryName) {
        return false;
    }

    @Override
    public void appendToBootstrapClassPath(JarFile jarFile) {

    }


    private CtClass getCtClass(InstrumentContext instrumentContext, ClassLoader classLoader, String className, byte[] classfileBuffer) {
        final NamedClassPool classPool = getClassPool(classLoader);
        try {
            if (classfileBuffer == null) {
                logger.info("classFileBuffer is null className:{}", className);
                return classPool.get(className);
            } else {
                final ClassPool contextCassPool = getContextClassPool(instrumentContext, classPool, className, classfileBuffer);
                return contextCassPool.get(className);
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(className + " class not found. Cause:" + e.getMessage(), e);
        }
    }

    private ClassPool getContextClassPool(InstrumentContext instrumentContext, NamedClassPool parent, String jvmInternalClassName, byte[] classfileBuffer) throws NotFoundException {
        final ClassPool contextCassPool = new ClassPool(parent);
        contextCassPool.childFirstLookup = true;

        final String javaName = JavaAssistUtils.jvmNameToJavaName(jvmInternalClassName);
        final ClassPath byteArrayClassPath = new ByteArrayClassPath(javaName, classfileBuffer);
        contextCassPool.insertClassPath(byteArrayClassPath);

        // append plugin jar for jboss
        // plugin class not found in jboss classLoader
        if (instrumentContext instanceof PluginInstrumentContext) {
            final PluginConfig pluginConfig = ((PluginInstrumentContext) instrumentContext).getPluginConfig();
            if (pluginConfig != null) {
                String jarPath = pluginConfig.getPluginJar().getPath();
                contextCassPool.appendClassPath(jarPath);
            }
        }
        return contextCassPool;
    }

    public NamedClassPool getClassPool(ClassLoader classLoader) {
        return childClassPool.getClassPool(classLoader);
    }

}
