package com.osight.monitor.boot;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class LoadState {
    private final AtomicBoolean state = new AtomicBoolean(false);

    public boolean start() {
        return state.compareAndSet(false, true);
    }
}
