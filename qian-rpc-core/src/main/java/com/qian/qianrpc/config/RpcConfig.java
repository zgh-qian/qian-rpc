package com.qian.qianrpc.config;

import com.qian.qianrpc.loadbalancer.LoadBalancerKeys;
import com.qian.qianrpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {
    /**
     * 名称
     */
    private String name = "qian-rpc";

    /**
     * 版本
     */
    private String version = "1.0";

    /**
     * 服务端主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务端端口
     */
    private int serverPort = 8080;

    /**
     * 模拟调用
     */
    private boolean mock = false;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;
}
