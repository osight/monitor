package com.osight.monitor.profiler.instrument.classpool;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.MapMaker;
import com.osight.monitor.core.util.ClassLoaderUtils;

import javassist.ClassPath;
import javassist.LoaderClassPath;


/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class IsolateMultipleClassPool implements MultipleClassPool {
    private static final AtomicInteger ID = new AtomicInteger();
    static final boolean DEFAULT_CHILD_FIRST_LOOKUP = true;

    private static final ClassLoader AGENT_CLASS_LOADER = IsolateMultipleClassPool.class.getClassLoader();

    private final NamedClassPool rootClassPool;

    private final ConcurrentMap<ClassLoader, NamedClassPool> classPoolMap;

    private final boolean childFirstLookup;

    public IsolateMultipleClassPool(ClassPoolHandler systemClassPoolHandler) {
        this(DEFAULT_CHILD_FIRST_LOOKUP, systemClassPoolHandler);
    }

    public IsolateMultipleClassPool(boolean childFirstLookup, ClassPoolHandler rootClassPoolHandler) {
        this.rootClassPool = createRootClassPool(rootClassPoolHandler);
        final MapMaker mapMaker = new MapMaker();
        mapMaker.weakKeys();
        this.classPoolMap = mapMaker.makeMap();
        this.childFirstLookup = childFirstLookup;
    }


    @Override
    public NamedClassPool getClassPool(ClassLoader classLoader) {
        if (ClassLoaderUtils.isJvmClassLoader(classLoader)) {
            return rootClassPool;
        }
        final NamedClassPool hit = this.classPoolMap.get(classLoader);
        if (hit != null) {
            return hit;
        }
        NamedClassPool newClassPool = createClassPool(classLoader);
        return put(classLoader, newClassPool);
    }

    private NamedClassPool put(ClassLoader classLoader, NamedClassPool classPool) {
        final NamedClassPool exist = this.classPoolMap.putIfAbsent(classLoader, classPool);
        if (exist != null) {
            return exist;
        }
        return classPool;
    }

    private NamedClassPool createClassPool(ClassLoader classLoader) {
        String classLoaderName = classLoader.toString();
        NamedClassPool newClassPool = new NamedClassPool(rootClassPool, classLoaderName + "-" + getNextId());
        if (childFirstLookup) {
            newClassPool.childFirstLookup = true;
        }
        final ClassPath classPath = new LoaderClassPath(classLoader);
        newClassPool.appendClassPath(classPath);

        return newClassPool;
    }

    private int getNextId() {
        return ID.getAndIncrement();
    }

    private NamedClassPool createRootClassPool(ClassPoolHandler rootClassPoolHandler) {
        NamedClassPool systemClassPool = new NamedClassPool("rootClassPool");
        systemClassPool.appendSystemPath();
        if (rootClassPoolHandler != null) {
            rootClassPoolHandler.handleClassPool(systemClassPool);

        }
        return systemClassPool;
    }

    public interface ClassPoolHandler {
        void handleClassPool(NamedClassPool classPool);
    }
}
