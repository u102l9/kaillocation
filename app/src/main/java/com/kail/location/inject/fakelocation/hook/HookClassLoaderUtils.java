package com.kail.location.inject.fakelocation.hook;

import com.kail.location.inject.utils.ReflectionUtils;

public class HookClassLoaderUtils {
    public static Class loadClass(ClassLoader classLoader, String className) {
        try {
            return classLoader == null ? ReflectionUtils.forName(className) : ReflectionUtils.loadClass(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
