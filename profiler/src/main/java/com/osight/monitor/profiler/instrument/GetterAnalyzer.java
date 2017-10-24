package com.osight.monitor.profiler.instrument;

import java.lang.reflect.Method;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class GetterAnalyzer {
    public GetterDetails analyze(Class<?> getterType) {
        if (!getterType.isInterface()) {
            throw new IllegalArgumentException("getterType " + getterType + "is not an interface");
        }

        Method[] methods = getterType.getDeclaredMethods();

        if (methods.length != 1) {
            throw new IllegalArgumentException("Getter interface must have only one method: " + getterType.getName());
        }

        Method getter = methods[0];

        if (getter.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Getter interface method must be no-args and non-void: " + getterType.getName());
        }

        Class<?> fieldType = getter.getReturnType();

        if (fieldType == void.class || fieldType == Void.class) {
            throw new IllegalArgumentException("Getter interface method must be no-args and non-void: " + getterType.getName());
        }

        return new GetterDetails(getter, fieldType);
    }

    public static final class GetterDetails {
        private final Method getter;
        private final Class<?> fieldType;

        public GetterDetails(Method getter, Class<?> fieldType) {
            this.getter = getter;
            this.fieldType = fieldType;
        }

        public Method getGetter() {
            return getter;
        }

        public Class<?> getFieldType() {
            return fieldType;
        }
    }
}
