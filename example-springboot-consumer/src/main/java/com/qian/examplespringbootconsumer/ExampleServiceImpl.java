package com.qian.examplespringbootconsumer;

import com.qian.example.common.model.User;
import com.qian.example.common.service.UserService;
import com.qian.qianrpcspringbootstarter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {
    @RpcReference
    private UserService userService;

    public void test() {
        User user = new User();
        user.setName("qian");
        User serviceUser = userService.getUser(user);
        System.out.println(serviceUser.getName());
    }
}
