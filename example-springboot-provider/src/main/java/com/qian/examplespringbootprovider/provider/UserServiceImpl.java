package com.qian.examplespringbootprovider.provider;

import com.qian.example.common.model.User;
import com.qian.example.common.service.UserService;
import com.qian.qianrpcspringbootstarter.annotation.RpcService;
import org.springframework.stereotype.Service;

@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("user: " + user.getName());
        return user;
    }
}
