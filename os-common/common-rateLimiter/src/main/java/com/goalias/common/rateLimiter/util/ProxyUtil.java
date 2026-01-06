package com.goalias.common.rateLimiter.util;

public class ProxyUtil {
    public static Class<?> getUserClass(Class<?> clazz) {//拿到初始类
        if (isCglibProxyClass(clazz)) {
            Class<?> superclass = clazz.getSuperclass();
            return getUserClass(superclass);
        }
        return clazz;
    }

    public static boolean isCglibProxyClass(Class<?> clazz) {
        return (clazz != null && clazz.getName().contains("$$"));
    }
}
