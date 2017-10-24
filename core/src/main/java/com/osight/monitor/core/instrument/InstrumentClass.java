package com.osight.monitor.core.instrument;

import java.util.List;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface InstrumentClass {
    boolean isInterface();

    String getName();

    String getSuperClass();

    String[] getInterfaces();

    InstrumentMethod getConstructor(String... parameterTypes);

    List<InstrumentMethod> getDeclaredMethods();

    List<InstrumentMethod> getDeclaredMethods(MethodFilter filter);

    InstrumentMethod getDeclaredMethod(String name, String... parameterTypes);

    ClassLoader getClassLoader();


    boolean isInterceptable();

    boolean hasConstructor(String... parameterTypes);

    boolean hasDeclaredMethod(String methodName, String... parameterTypes);

    boolean hasMethod(String methodName, String... parameterTypes);

    boolean hasField(String name, String type);

    boolean hasField(String name);


    void addField(String accessorTypeName) throws InstrumentException;

    void addGetter(String getterTypeName, String fieldName) throws InstrumentException;

    void addSetter(String setterTypeName, String fieldName) throws InstrumentException;

    void addSetter(String setterTypeName, String fieldName, boolean removeFinal) throws InstrumentException;

    InstrumentMethod addDelegatorMethod(String methodName, String... paramTypes) throws InstrumentException;

    byte[] toBytecode() throws InstrumentException;
}
