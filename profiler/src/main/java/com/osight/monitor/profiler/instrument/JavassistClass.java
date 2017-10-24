package com.osight.monitor.profiler.instrument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osight.monitor.core.instrument.InstrumentClass;
import com.osight.monitor.core.instrument.InstrumentContext;
import com.osight.monitor.core.instrument.InstrumentException;
import com.osight.monitor.core.instrument.InstrumentMethod;
import com.osight.monitor.core.instrument.MethodFilter;
import com.osight.monitor.profiler.instrument.AccessorAnalyzer.AccessorDetails;
import com.osight.monitor.profiler.instrument.GetterAnalyzer.GetterDetails;
import com.osight.monitor.profiler.instrument.SetterAnalyzer.SetterDetails;
import com.osight.monitor.profiler.util.JavaAssistUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class JavassistClass implements InstrumentClass {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final InstrumentContext pluginContext;
    private final ClassLoader classLoader;

    private final CtClass ctClass;
    private static final String FIELD_PREFIX = "_$PINPOINT$_";
    private static final String SETTER_PREFIX = "_$PINPOINT$_set";
    private static final String GETTER_PREFIX = "_$PINPOINT$_get";

    JavassistClass(InstrumentContext pluginContext, ClassLoader classLoader, CtClass ctClass) {
        this.pluginContext = pluginContext;
        this.classLoader = classLoader;
        this.ctClass = ctClass;
    }


    @Override
    public boolean isInterface() {
        return this.ctClass.isInterface();
    }

    @Override
    public String getName() {
        return ctClass.getName();
    }

    @Override
    public String getSuperClass() {
        return this.ctClass.getClassFile2().getSuperclass();
    }

    @Override
    public String[] getInterfaces() {
        return this.ctClass.getClassFile2().getInterfaces();
    }

    @Override
    public InstrumentMethod getConstructor(String... parameterTypes) {
        CtConstructor constructor = getCtConstructor0(parameterTypes);
        if (constructor == null) {
            return null;
        }

        return new JavassistMethod(pluginContext, this, constructor);
    }

    private CtConstructor getCtConstructor0(String[] parameterTypes) {
        final String jvmSignature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
            final String descriptor = constructor.getMethodInfo2().getDescriptor();
            if (descriptor.startsWith(jvmSignature) && constructor.isConstructor()) {
                return constructor;
            }
        }
        return null;
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods() {
        return getDeclaredMethods(new MethodFilter() {
            @Override
            public boolean accept(InstrumentMethod method) {
                return true;
            }
        });
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods(MethodFilter methodFilter) {
        final CtMethod[] declaredMethod = ctClass.getDeclaredMethods();
        final List<InstrumentMethod> candidateList = new ArrayList<InstrumentMethod>(declaredMethod.length);
        for (CtMethod ctMethod : declaredMethod) {
            final InstrumentMethod method = new JavassistMethod(pluginContext, this, ctMethod);
            if (methodFilter.accept(method)) {
                candidateList.add(method);
            }
        }

        return candidateList;
    }

    @Override
    public InstrumentMethod getDeclaredMethod(String name, String... parameterTypes) {
        CtMethod method = getCtMethod0(ctClass, name, parameterTypes);
        if (method == null) {
            return null;
        }
        return new JavassistMethod(pluginContext, this, method);
    }

    private static CtMethod getCtMethod0(CtClass ctClass, String methodName, String[] parameterTypes) {
        final String jvmSignature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            final String descriptor = method.getMethodInfo2().getDescriptor();
            if (descriptor.startsWith(jvmSignature)) {
                return method;
            }
        }

        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public boolean isInterceptable() {
        return !ctClass.isInterface() && !ctClass.isAnnotation() && !ctClass.isModified();
    }

    @Override
    public boolean hasConstructor(String... parameterTypes) {
        final String signature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes, "void");
        try {
            CtConstructor c = ctClass.getConstructor(signature);
            return c != null;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean hasDeclaredMethod(String methodName, String... args) {
        return getCtMethod0(ctClass, methodName, args) != null;
    }

    @Override
    public boolean hasMethod(String methodName, String... parameterTypes) {
        final String jvmSignature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);

        for (CtMethod method : ctClass.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            final String descriptor = method.getMethodInfo2().getDescriptor();
            if (descriptor.startsWith(jvmSignature)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasField(String name, String type) {
        try {
            String vmType = null;
            if (type != null) {
                vmType = JavaAssistUtils.toJvmSignature(type);
            }
            ctClass.getField(name, vmType);
        } catch (NotFoundException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasField(String name) {
        return hasField(name, null);
    }

    @Override
    public void addField(String accessorTypeName) throws InstrumentException {
        addField0(accessorTypeName, null);
    }

    private void addField0(String accessorTypeName, String initValExp) throws InstrumentException {
        try {
            Class<?> accessorType = pluginContext.injectClass(classLoader, accessorTypeName);
            final AccessorAnalyzer accessorAnalyzer = new AccessorAnalyzer();
            final AccessorDetails accessorDetails = accessorAnalyzer.analyze(accessorType);

            Class<?> fieldType = accessorDetails.getFieldType();
            String fieldTypeName = JavaAssistUtils.javaClassNameToObjectName(fieldType.getName());

            final CtField newField = CtField.make("private " + fieldTypeName + " " + FIELD_PREFIX + JavaAssistUtils.javaClassNameToVariableName(accessorTypeName) + ";", ctClass);

            if (initValExp == null) {
                ctClass.addField(newField);
            } else {
                ctClass.addField(newField, initValExp);
            }
            final CtClass accessorInterface = getCtClass(accessorTypeName);
            ctClass.addInterface(accessorInterface);

            CtMethod getterMethod = CtNewMethod.getter(accessorDetails.getGetter().getName(), newField);
            ctClass.addMethod(getterMethod);

            CtMethod setterMethod = CtNewMethod.setter(accessorDetails.getSetter().getName(), newField);
            ctClass.addMethod(setterMethod);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add field with accessor [" + accessorTypeName + "]. Cause:" + e.getMessage(), e);
        }
    }

    @Override
    public void addGetter(String getterTypeName, String fieldName) throws InstrumentException {
        try {

            Class<?> getterType = pluginContext.injectClass(classLoader, getterTypeName);
            GetterAnalyzer getterAnalyzer = new GetterAnalyzer();
            GetterDetails getterDetails = getterAnalyzer.analyze(getterType);

            CtField field = ctClass.getField(fieldName);
            String fieldTypeName = JavaAssistUtils.javaClassNameToObjectName(getterDetails.getFieldType().getName());

            if (!field.getType().getName().equals(fieldTypeName)) {
                throw new IllegalArgumentException("Return type of the getter is different with the field type. getterMethod: " + getterDetails.getGetter() + ", fieldType: " + field.getType().getName());
            }

            CtMethod getterMethod = CtNewMethod.getter(getterDetails.getGetter().getName(), field);

            if (getterMethod.getDeclaringClass() != ctClass) {
                getterMethod = CtNewMethod.copy(getterMethod, ctClass, null);
            }

            ctClass.addMethod(getterMethod);

            CtClass ctInterface = getCtClass(getterTypeName);
            ctClass.addInterface(ctInterface);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add getter: " + getterTypeName, e);
        }
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName) throws InstrumentException {
        this.addSetter(setterTypeName, fieldName, false);
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName, boolean removeFinalFlag) throws InstrumentException {
        try {
            Class<?> setterType = pluginContext.injectClass(classLoader, setterTypeName);

            SetterAnalyzer setterAnalyzer = new SetterAnalyzer();
            SetterDetails setterDetails = setterAnalyzer.analyze(setterType);

            CtField field = ctClass.getField(fieldName);
            String fieldTypeName = JavaAssistUtils.javaClassNameToObjectName(setterDetails.getFieldType().getName());

            if (!field.getType().getName().equals(fieldTypeName)) {
                throw new IllegalArgumentException("Argument type of the setter is different with the field type. setterMethod: " + setterDetails.getSetter() + ", fieldType: " + field.getType().getName());
            }

            final int originalModifiers = field.getModifiers();
            if (Modifier.isStatic(originalModifiers)) {
                throw new IllegalArgumentException("Cannot add setter to static fields. setterMethod: " + setterDetails.getSetter().getName() + ", fieldName: " + fieldName);
            }

            boolean finalRemoved = false;
            if (Modifier.isFinal(originalModifiers)) {
                if (!removeFinalFlag) {
                    throw new IllegalArgumentException("Cannot add setter to final field. setterMethod: " + setterDetails.getSetter().getName() + ", fieldName: " + fieldName);
                } else {
                    final int modifiersWithFinalRemoved = Modifier.clear(originalModifiers, Modifier.FINAL);
                    field.setModifiers(modifiersWithFinalRemoved);
                    finalRemoved = true;
                }
            }

            try {
                CtMethod setterMethod = CtNewMethod.setter(setterDetails.getSetter().getName(), field);
                if (setterMethod.getDeclaringClass() != ctClass) {
                    setterMethod = CtNewMethod.copy(setterMethod, ctClass, null);
                }
                ctClass.addMethod(setterMethod);

                CtClass ctInterface = getCtClass(setterTypeName);
                ctClass.addInterface(ctInterface);
            } catch (Exception e) {
                if (finalRemoved) {
                    field.setModifiers(originalModifiers);
                }
                throw e;
            }
        } catch (Exception e) {
            throw new InstrumentException("Failed to add setter: " + setterTypeName, e);
        }
    }

    @Override
    public InstrumentMethod addDelegatorMethod(String methodName, String... paramTypes) throws InstrumentException {
        if (getCtMethod0(ctClass, methodName, paramTypes) != null) {
            throw new InstrumentException(getName() + "already have method(" + methodName + ").");
        }

        try {
            final CtClass superClass = ctClass.getSuperclass();
            CtMethod superMethod = getCtMethod0(superClass, methodName, paramTypes);
            CtMethod delegatorMethod = CtNewMethod.delegator(superMethod, ctClass);
            ctClass.addMethod(delegatorMethod);

            return new JavassistMethod(pluginContext, this, delegatorMethod);
        } catch (NotFoundException ex) {
            throw new InstrumentException(getName() + "don't have super class(" + getSuperClass() + "). Cause:" + ex.getMessage(), ex);
        } catch (CannotCompileException ex) {
            throw new InstrumentException(methodName + " addDelegatorMethod fail. Cause:" + ex.getMessage(), ex);
        }
    }

    @Override
    public byte[] toBytecode() throws InstrumentException {
        try {
            byte[] bytes = ctClass.toBytecode();
            ctClass.detach();
            return bytes;
        } catch (IOException e) {
            logger.info("IoException class:{} Caused:{}", ctClass.getName(), e.getMessage(), e);
        } catch (CannotCompileException e) {
            logger.info("CannotCompileException class:{} Caused:{}", ctClass.getName(), e.getMessage(), e);
        }
        return null;
    }

    private CtClass getCtClass(String className) throws NotFoundException {
        final ClassPool classPool = ctClass.getClassPool();
        return classPool.get(className);
    }
}
