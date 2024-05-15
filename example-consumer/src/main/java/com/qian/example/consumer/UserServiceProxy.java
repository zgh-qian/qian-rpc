package com.qian.example.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.qian.example.common.model.User;
import com.qian.example.common.service.UserService;
import com.qian.qianrpc.model.RpcRequest;
import com.qian.qianrpc.model.RpcResponse;
import com.qian.qianrpc.serializer.JdkSerializer;
import com.qian.qianrpc.serializer.Serializer;

import java.io.IOException;

/**
 * 静态代理
 */
public class UserServiceProxy implements UserService {
    @Override
    public User getUser(User user) {
        // 指定序列化器
        Serializer serializer = new JdkSerializer();
        // 发起请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try {
            byte[] serialized = serializer.serialize(rpcRequest);
            byte[] result;
            HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(serialized)
                    .execute();
            result = httpResponse.bodyBytes();
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
