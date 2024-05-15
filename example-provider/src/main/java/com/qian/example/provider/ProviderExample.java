package com.qian.example.provider;

import com.qian.example.common.service.UserService;
import com.qian.qianrpc.RpcApplication;
import com.qian.qianrpc.registry.LocalRegistry;
import com.qian.qianrpc.server.HttpServer;
import com.qian.qianrpc.server.VertxHttpServer;

public class ProviderExample {
    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 启动 Web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
