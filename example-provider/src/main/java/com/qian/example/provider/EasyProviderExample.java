package com.qian.example.provider;

import com.qian.example.common.service.UserService;
import com.qian.qianrpc.registry.LocalRegistry;
import com.qian.qianrpc.server.HttpServer;
import com.qian.qianrpc.server.http.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 启动 Web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
