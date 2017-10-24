package com.osight.monitor.profiler.instrument;

import com.osight.monitor.core.instrument.InstrumentClass;
import com.osight.monitor.core.instrument.InstrumentContext;
import com.osight.monitor.core.instrument.InstrumentMethod;

import javassist.CtBehavior;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class JavassistMethod implements InstrumentMethod {
    private final InstrumentContext pluginContext;
    private final CtBehavior behavior;
    private final InstrumentClass declaringClass;


    JavassistMethod(InstrumentContext pluginContext, InstrumentClass declaringClass, CtBehavior behavior) {
        this.pluginContext = pluginContext;
        this.behavior = behavior;
        this.declaringClass = declaringClass;
    }
}
