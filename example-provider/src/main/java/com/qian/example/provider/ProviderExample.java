package com.qian.example.provider;

import com.qian.example.common.service.UserService;
import com.qian.qianrpc.RpcApplication;
import com.qian.qianrpc.bootstrap.ProviderBootstrap;
import com.qian.qianrpc.config.RegistryConfig;
import com.qian.qianrpc.config.RpcConfig;
import com.qian.qianrpc.model.ServiceMetaInfo;
import com.qian.qianrpc.model.ServiceRegisterInfo;
import com.qian.qianrpc.registry.LocalRegistry;
import com.qian.qianrpc.registry.Registry;
import com.qian.qianrpc.registry.RegistryFactory;
import com.qian.qianrpc.server.HttpServer;
import com.qian.qianrpc.server.http.VertxHttpServer;
import com.qian.qianrpc.server.tcp.VertxTcpServer;

import java.util.ArrayList;
import java.util.List;

public class ProviderExample {
    public static void main(String[] args) {
        // 要注册的服务
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        ServiceRegisterInfo serviceRegisterInfo = new ServiceRegisterInfo(UserService.class.getName(), UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);
        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }
}
