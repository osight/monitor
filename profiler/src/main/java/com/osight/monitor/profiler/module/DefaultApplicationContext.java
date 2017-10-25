package com.osight.monitor.profiler.module;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osight.monitor.boot.AgentOption;
import com.osight.monitor.profiler.instrument.InstrumentEngine;
import com.osight.monitor.profiler.instrument.JavassistEngine;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class DefaultApplicationContext implements ApplicationContext {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AgentOption agentOption;
    private final Instrumentation instrumentation;
    private final InstrumentEngine instrumentEngine;

    public DefaultApplicationContext(AgentOption agentOption) {
        this.agentOption = agentOption;
        this.instrumentation = agentOption.getInstrumentation();
        this.instrumentEngine = new JavassistEngine(this.instrumentation, agentOption.getBootstrapJarPaths());
    }

    @Override
    public void start() {

    }

    @Override
    public void close() {

    }
}
