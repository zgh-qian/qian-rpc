package com.qian.example.consumer;

import com.qian.example.common.model.User;
import com.qian.example.common.service.UserService;
import com.qian.qianrpc.config.RpcConfig;
import com.qian.qianrpc.proxy.ServiceProxyFactory;
import com.qian.qianrpc.utils.ConfigUtils;

/**
 * 简易消费者示例
 */
public class EasyConsumerExample {
    public static void main(String[] args) {
        // 需要获取 UserService 的实现类对象
        //UserService userService = null;
        // 静态代理
        // UserService userService = new UserServiceProxy();
        // 动态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("zgh");
        // 调用服务
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user not found");
        }
    }
}
