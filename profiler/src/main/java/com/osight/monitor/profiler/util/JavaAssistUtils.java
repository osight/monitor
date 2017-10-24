package com.osight.monitor.profiler.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class JavaAssistUtils {
    private static final Map<String, String> PRIMITIVE_JAVA_TO_JVM = createPrimitiveJavaToJvmMap();
    private static final String ARRAY = "[]";

    private static Map<String, String> createPrimitiveJavaToJvmMap() {
        final Map<String, String> primitiveJavaToJvm = new HashMap<String, String>();
        primitiveJavaToJvm.put("byte", "B");
        primitiveJavaToJvm.put("char", "C");
        primitiveJavaToJvm.put("double", "D");
        primitiveJavaToJvm.put("float", "F");
        primitiveJavaToJvm.put("int", "I");
        primitiveJavaToJvm.put("long", "J");
        primitiveJavaToJvm.put("short", "S");
        primitiveJavaToJvm.put("void", "V");
        primitiveJavaToJvm.put("boolean", "Z");
        return primitiveJavaToJvm;
    }

    private JavaAssistUtils() {
    }

    public static String javaClassNameToObjectName(String javaClassName) {
        final char scheme = javaClassName.charAt(0);
        switch (scheme) {
            case '[':
                return toArrayType(javaClassName);
            default:
                return javaClassName;
        }
    }

    private static String toArrayType(String description) {
        final int arraySize = getArraySize(description);
        final String objectType = byteCodeSignatureToObjectType(description, arraySize);
        return arrayType(objectType, arraySize);
    }

    private static String byteCodeSignatureToObjectType(String signature, int startIndex) {
        final char scheme = signature.charAt(startIndex);
        switch (scheme) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'V':
                return "void";
            case 'Z':
                return "boolean";
            case 'L':
                return toObjectType(signature, startIndex + 1);
            case '[': {
                return toArrayType(signature);
            }
        }
        throw new IllegalArgumentException("invalid signature :" + signature);
    }

    private static String toObjectType(String signature, int startIndex) {
        final String assistClass = signature.substring(startIndex, signature.length() - 1);
        final String objectName = jvmNameToJavaName(assistClass);
        if (objectName.isEmpty()) {
            throw new IllegalArgumentException("invalid signature. objectName not found :" + signature);
        }
        return objectName;
    }

    public static String jvmNameToJavaName(String jvmName) {
        return jvmName.replace('/', '.');
    }

    public static String javaClassNameToVariableName(String javaClassName) {
        if (javaClassName == null) {
            throw new NullPointerException("java class name must not be null");
        }

        return javaClassName.replace('.', '_').replace('$', '_').replace('[', '_').replace(']', '_');
    }

    private static String arrayType(String objectType, int arraySize) {
        final int arrayStringLength = ARRAY.length() * arraySize;
        StringBuilder sb = new StringBuilder(objectType.length() + arrayStringLength);
        sb.append(objectType);
        for (int i = 0; i < arraySize; i++) {
            sb.append(ARRAY);
        }
        return sb.toString();
    }


    private static int getArraySize(String description) {
        if (description == null || description.length() == 0) {
            return 0;
        }
        int arraySize = 0;
        for (int i = 0; i < description.length(); i++) {
            final char c = description.charAt(i);
            if (c == '[') {
                arraySize++;
            } else {
                break;
            }
        }
        return arraySize;
    }

    public static String javaTypeToJvmSignature(String[] javaTypeArray, String returnType) {
        if (returnType == null) {
            throw new NullPointerException("returnType must not be null");
        }
        final String parameterSignature = javaTypeToJvmSignature(javaTypeArray);
        final StringBuilder sb = new StringBuilder(parameterSignature.length() + 8);
        sb.append(parameterSignature);
        sb.append(toJvmSignature(returnType));
        return sb.toString();
    }

    public static String javaTypeToJvmSignature(String[] javaTypeArray) {
        if (javaTypeArray == null || javaTypeArray.length == 0) {
            return "()";
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        for (String javaType : javaTypeArray) {
            final String jvmSignature = toJvmSignature(javaType);
            buffer.append(jvmSignature);
        }
        buffer.append(')');
        return buffer.toString();
    }

    public static String toJvmSignature(String javaType) {
        if (javaType == null) {
            throw new NullPointerException("javaType must not be null");
        }
        if (javaType.isEmpty()) {
            throw new IllegalArgumentException("invalid javaType. \"\"");
        }

        final int javaObjectArraySize = getJavaObjectArraySize(javaType);
        final int javaArrayLength = javaObjectArraySize * 2;
        String pureJavaType;
        if (javaObjectArraySize != 0) {
            // pure java
            pureJavaType = javaType.substring(0, javaType.length() - javaArrayLength);
        } else {
            pureJavaType = javaType;
        }
        final String signature = PRIMITIVE_JAVA_TO_JVM.get(pureJavaType);
        if (signature != null) {
            // primitive type
            return appendJvmArray(signature, javaObjectArraySize);
        }
        return toJvmObject(javaObjectArraySize, pureJavaType);
    }

    private static String appendJvmArray(String signature, int javaObjectArraySize) {
        if (javaObjectArraySize == 0) {
            return signature;
        }
        StringBuilder sb = new StringBuilder(signature.length() + javaObjectArraySize);
        for (int i = 0; i < javaObjectArraySize; i++) {
            sb.append('[');
        }
        sb.append(signature);
        return sb.toString();
    }

    private static String toJvmObject(int javaObjectArraySize, String pureJavaType) {
        final StringBuilder buffer = new StringBuilder(pureJavaType.length() + javaObjectArraySize + 2);
        for (int i = 0; i < javaObjectArraySize; i++) {
            buffer.append('[');
        }
        buffer.append('L');
        buffer.append(javaNameToJvmName(pureJavaType));
        buffer.append(';');
        return buffer.toString();
    }

    public static String javaNameToJvmName(String javaName) {
        return javaName.replace('.', '/');
    }

    static int getJavaObjectArraySize(String javaType) {
        if (javaType.isEmpty()) {
            return 0;
        }
        final int endIndex = javaType.length() - 1;
        final char checkEndArrayExist = javaType.charAt(endIndex);
        if (checkEndArrayExist != ']') {
            return 0;
        }
        int arraySize = 0;
        for (int i = endIndex; i > 0; i = i - 2) {
            final char arrayEnd = javaType.charAt(i);
            final char arrayStart = javaType.charAt(i - 1);
            if (arrayStart == '[' && arrayEnd == ']') {
                arraySize++;
            } else {
                return arraySize;
            }
        }
        return arraySize;
    }
}
