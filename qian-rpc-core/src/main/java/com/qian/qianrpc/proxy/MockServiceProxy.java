package com.qian.qianrpc.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock 服务代理
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据方法的返回值类型，生成默认值对象
        Class<?> methodReturnType = method.getReturnType();
        log.info("mock invoke : {}", method.getName());
        return getDefaultObject(methodReturnType);
    }

    /**
     * 根据类型生成默认值对象
     *
     * @param type 类型
     * @return 默认值对象
     */
    private Object getDefaultObject(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                return false;
            } else if (type == int.class) {
                return 0;
            } else if (type == long.class) {
                return 0L;
            } else if (type == double.class) {
                return 0.0;
            } else if (type == float.class) {
                return 0.0f;
            } else if (type == byte.class) {
                return (byte) 0;
            } else if (type == short.class) {
                return (short) 0;
            } else if (type == char.class) {
                return '\u0000';
            } else {
                return null;
            }
        }
        return null;
    }
}
