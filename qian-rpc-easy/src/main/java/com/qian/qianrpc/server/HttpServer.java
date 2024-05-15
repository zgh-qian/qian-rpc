package com.qian.qianrpc.server;

/**
 * HTTP 服务器接口
 */
public interface HttpServer {
    /**
     * 启动 HTTP 服务器
     *
     * @param port 端口号
     */
    void doStart(int port);
}
