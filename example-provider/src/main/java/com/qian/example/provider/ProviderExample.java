package com.qian.example.provider;

import com.qian.example.common.service.UserService;
import com.qian.qianrpc.RpcApplication;
import com.qian.qianrpc.config.RegistryConfig;
import com.qian.qianrpc.config.RpcConfig;
import com.qian.qianrpc.model.ServiceMetaInfo;
import com.qian.qianrpc.registry.LocalRegistry;
import com.qian.qianrpc.registry.Registry;
import com.qian.qianrpc.registry.RegistryFactory;
import com.qian.qianrpc.server.HttpServer;
import com.qian.qianrpc.server.http.VertxHttpServer;
import com.qian.qianrpc.server.tcp.VertxTcpServer;

public class ProviderExample {
    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();
        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);
        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        startTCPServer();
    }

    private static void startHTTPServer() {
        // 启动 HTTP 服务
        HttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }

    private static void startTCPServer() {
        // 启动 HTTP 服务
        HttpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
