package com.qian.qianrpc.config;

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
}
