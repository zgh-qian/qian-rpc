package com.qian.qianrpc.serializer;

import com.qian.qianrpc.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {
    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     *
     * @param key 序列化器的key
     * @return 序列化器实例
     */
    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
