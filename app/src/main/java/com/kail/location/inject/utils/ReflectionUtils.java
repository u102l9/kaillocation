package com.kail.location.inject.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtils {
    private static Method classGetDeclaredMethod;
    private static Method classForName;
    private static Method classForNameWithLoader;

    static {
        try {
            classGetDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            classForName = Class.class.getDeclaredMethod("forName", String.class);
            classForNameWithLoader = Class.class.getDeclaredMethod("forName", String.class, Boolean.TYPE, ClassLoader.class);
        } catch (Exception unused) {
        }
    }

    public static Class loadClass(ClassLoader classLoader, String className) {
        return loadClass(className, true, classLoader);
    }

    public static Class forName(String className) throws ClassNotFoundException {
        Method method = classForName;
        if (method != null) {
            try {
                return (Class) method.invoke(null, className);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Class.forName(className);
    }

    public static Class loadClass(String className, boolean initialize, ClassLoader classLoader) {
        try {
            Method method = classForNameWithLoader;
            if (method != null) {
                try {
                    return (Class) method.invoke(null, className, Boolean.valueOf(initialize), classLoader);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Class.forName(className, initialize, classLoader);
                }
            }
            return Class.forName(className, initialize, classLoader);
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static <E> E getFieldValue(Object instance, Class declaringClass, String fieldName) {
        try {
            Field field = findField(declaringClass, fieldName);
            if (field != null) {
                field.setAccessible(true);
                return (E) field.get(instance);
            }
            throw new NoSuchFieldException(declaringClass.toString() + " " + fieldName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <E> E invokeMethod(Object receiver, Class declaringClass, String methodName, Class[] parameterTypes, Object[] args) throws Exception {
        if (declaringClass == null) {
            throw new NullPointerException("ClassInvoker: Invoke class is null.");
        }
        Method method = null;
        Class currentClass = declaringClass;
        while (currentClass != null && method == null) {
            try {
                Method getDeclaredMethod = classGetDeclaredMethod;
                if (getDeclaredMethod != null) {
                    method = (Method) getDeclaredMethod.invoke(currentClass, methodName, parameterTypes);
                } else {
                    method = currentClass.getDeclaredMethod(methodName, parameterTypes);
                }
            } catch (NoSuchMethodException unused) {
                currentClass = currentClass.getSuperclass();
            }
        }
        if (method != null) {
            method.setAccessible(true);
            return (E) method.invoke(receiver, args);
        }
        throw new NoSuchMethodException(methodName + " " + Arrays.toString(parameterTypes));
    }

    public static <E> E invokeMethod(Object receiver, String className, String methodName, Class[] parameterTypes, Object[] args) throws Exception {
        return (E) invokeMethod(receiver, forName(className), methodName, parameterTypes, args);
    }

    public static <E> E newInstance(Class targetClass, Class[] parameterTypes, Object[] args) throws NoSuchMethodException {
        Constructor constructor;
        try {
            constructor = targetClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException unused) {
            constructor = targetClass.getDeclaredConstructor(parameterTypes);
        }
        constructor.setAccessible(true);
        try {
            return (E) constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldValue(Object instance, Class declaringClass, String fieldName, Object value) {
        try {
            Field field = findField(declaringClass, fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(instance, value);
                return;
            }
            throw new NoSuchFieldException(declaringClass.toString() + " " + fieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFinalFieldValue(Object instance, Class declaringClass, String fieldName, Object value) {
        try {
            Field field = findField(declaringClass, fieldName);
            if (field != null) {
                field.setAccessible(true);
                Field accessFlagsField = Field.class.getDeclaredField("accessFlags");
                accessFlagsField.setAccessible(true);
                accessFlagsField.setInt(field, field.getModifiers() & (-17));
                field.set(instance, value);
                accessFlagsField.setInt(field, field.getModifiers() & (-17));
                return;
            }
            throw new NoSuchFieldException(declaringClass.toString() + " " + fieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Field findField(Class declaringClass, String fieldName) {
        Class currentClass = declaringClass;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException unused) {
                currentClass = currentClass.getSuperclass();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
