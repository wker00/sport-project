package com.sportzone.utils;

/**
 * 线程局部变量工具类
 */
public class ThreadLocalUtil {

/**
 * 存放线程局部变量
 */
private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();

/**
 * 获取线程局部变量
 * @return
 * @param <T>
 */
@SuppressWarnings("unchecked")
public static <T> T get() {
    return (T) THREAD_LOCAL.get();
}

/**
 * 设置线程局部变量
 * @param value
 */
public static void set(Object value) {
    THREAD_LOCAL.set(value);
}

/**
 * 移除线程局部变量
 */
public static void remove() {
    THREAD_LOCAL.remove();
}

/**
 * 获取当前登录用户的ID
 * @return 用户ID
 */
public static Long getUserId() {
    Object value = THREAD_LOCAL.get();
    if (value instanceof Long) {
        return (Long) value;
    }
    if (value instanceof java.util.Map) {
        Object idObj = ((java.util.Map<?, ?>) value).get("id");
        if (idObj != null) {
            return ((Number) idObj).longValue();
        }
    }
    throw new RuntimeException("未获取到用户信息，请先登录");
}
}
