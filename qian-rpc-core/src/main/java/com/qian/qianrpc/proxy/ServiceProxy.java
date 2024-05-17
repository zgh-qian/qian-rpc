package com.qian.qianrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import com.qian.qianrpc.RpcApplication;
import com.qian.qianrpc.config.RpcConfig;
import com.qian.qianrpc.constant.RpcConstant;
import com.qian.qianrpc.fault.retry.RetryStrategy;
import com.qian.qianrpc.fault.retry.RetryStrategyFactory;
import com.qian.qianrpc.fault.tolerant.TolerantStrategy;
import com.qian.qianrpc.fault.tolerant.TolerantStrategyFactory;
import com.qian.qianrpc.loadbalancer.LoadBalancer;
import com.qian.qianrpc.loadbalancer.LoadBalancerFactory;
import com.qian.qianrpc.model.RpcRequest;
import com.qian.qianrpc.model.RpcResponse;
import com.qian.qianrpc.model.ServiceMetaInfo;
import com.qian.qianrpc.registry.Registry;
import com.qian.qianrpc.registry.RegistryFactory;
import com.qian.qianrpc.server.tcp.VertxTcpClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务代理（JDK动态代理）
 */
public class ServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        // 从注册中心获取服务提供者请求地址
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("暂无服务地址");
        }
        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        // 将调用方法名（请求路径）作为负载均衡参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        /*
        // http 请求
        // 指定序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        byte[] bodyBytes = serializer.serialize(rpcRequest);
        RpcResponse rpcResponse = doHttpRequest(selectedServiceMetaInfo, bodyBytes, serializer);
        */
        // rpc 请求
        // 使用重试机制
        RpcResponse rpcResponse;
        try {
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );
        } catch (Exception e) {
            // 容错机制
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }
        return rpcResponse.getData();
    }
}
