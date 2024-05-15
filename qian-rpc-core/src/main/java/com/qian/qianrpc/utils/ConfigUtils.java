package com.qian.qianrpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/*
 * 配置工具类
 */
public class ConfigUtils {
    /**
     * 加载配置对象
     *
     * @param tClass 配置类
     * @param prefix 前缀
     * @param <T>    配置类泛型
     * @return 配置对象
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象
     *
     * @param tClass      配置类
     * @param prefix      前缀
     * @param environment 环境
     * @param <T>         配置类泛型
     * @return 配置对象
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");
        Props props = new Props(configFileBuilder.toString());
        // 自动加载配置
        //props.autoLoad(true);
        return props.toBean(tClass, prefix);
    }
}
