package com.osight.monitor.profiler.instrument;

import java.util.jar.JarFile;

import com.osight.monitor.core.instrument.InstrumentClass;
import com.osight.monitor.core.instrument.InstrumentContext;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface InstrumentEngine {
    InstrumentClass getClass(InstrumentContext instrumentContext, ClassLoader classLoader, String classInternalName, byte[] classFileBuffer);

    boolean hasClass(ClassLoader classLoader, String classBinaryName);

    void appendToBootstrapClassPath(JarFile jarFile);
}
