package com.qian.example.consumer;

import com.qian.qianrpc.config.RpcConfig;
import com.qian.qianrpc.utils.ConfigUtils;

public class ConsumerExample {
    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);
    }
}
