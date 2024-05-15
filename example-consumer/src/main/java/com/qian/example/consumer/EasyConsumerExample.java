package com.qian.example.consumer;

import com.qian.example.common.model.User;
import com.qian.example.common.service.UserService;

/**
 * 简易消费者示例
 */
public class EasyConsumerExample {
    public static void main(String[] args) {
        // todo 需要获取 UserService 的实现类对象
        UserService userService = null;
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
