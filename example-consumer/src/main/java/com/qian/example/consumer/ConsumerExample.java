package com.qian.example.consumer;

import com.qian.example.common.model.User;
import com.qian.example.common.service.UserService;
import com.qian.qianrpc.config.RpcConfig;
import com.qian.qianrpc.proxy.ServiceProxyFactory;
import com.qian.qianrpc.utils.ConfigUtils;

public class ConsumerExample {
    public static void main(String[] args) {
        //RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("zgh");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        }else{
            System.out.println("user not found");
        }
        short number = userService.getNumber();
        System.out.println(number);
    }
}
