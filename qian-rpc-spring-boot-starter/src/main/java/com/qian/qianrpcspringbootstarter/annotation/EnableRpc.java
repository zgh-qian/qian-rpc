package com.qian.qianrpcspringbootstarter.annotation;

import com.qian.qianrpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.qian.qianrpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.qian.qianrpcspringbootstarter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 Rpc 注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {

    /**
     * 需要启动 server
     *
     * @return true:启动 server, false:不启动 server
     */
    boolean needServer() default true;
}
