package com.osight.monitor.profiler.instrument;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

import com.osight.monitor.core.instrument.InstrumentClass;
import com.osight.monitor.core.instrument.InstrumentContext;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class JavassistEngine implements InstrumentEngine {
    private final Instrumentation instrumentation;

    public JavassistEngine(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public InstrumentClass getClass(InstrumentContext instrumentContext, ClassLoader classLoader, String classInternalName, byte[] classFileBuffer) {
        return null;
    }

    @Override
    public boolean hasClass(ClassLoader classLoader, String classBinaryName) {
        return false;
    }

    @Override
    public void appendToBootstrapClassPath(JarFile jarFile) {

    }
}
