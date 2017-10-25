package com.osight.monitor.profiler;

import com.osight.monitor.boot.AgentOption;
import com.osight.monitor.core.Agent;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class DefaultAgent implements Agent {
    private final AgentOption agentOption;

    public DefaultAgent(AgentOption agentOption) {
        this.agentOption = agentOption;
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
