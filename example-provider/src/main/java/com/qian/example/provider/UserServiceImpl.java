package com.qian.example.provider;

import com.qian.example.common.model.User;
import com.qian.example.common.service.UserService;

/**
 * 用户服务实现类
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("user:" + user.getName());
        return user;
    }
}
