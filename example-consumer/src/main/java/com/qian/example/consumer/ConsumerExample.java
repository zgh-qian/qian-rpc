package com.qian.example.consumer;

import com.qian.example.common.model.User;
import com.qian.example.common.service.UserService;
import com.qian.qianrpc.bootstrap.ConsumerBootstrap;
import com.qian.qianrpc.config.RpcConfig;
import com.qian.qianrpc.proxy.ServiceProxyFactory;
import com.qian.qianrpc.utils.ConfigUtils;

public class ConsumerExample {
    public static void main(String[] args) {
        // 服务提供者初始化
        ConsumerBootstrap.init();
        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("qian");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user not found");
        }
    }
}
