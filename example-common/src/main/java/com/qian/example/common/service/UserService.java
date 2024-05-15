package com.qian.example.common.service;

import com.qian.example.common.model.User;

public interface UserService {
    /**
     * 获取用户
     *
     * @param user 用户对象
     * @return 用户对象
     */
    User getUser(User user);

    default short getNumber() {
        return 1;
    }
}
