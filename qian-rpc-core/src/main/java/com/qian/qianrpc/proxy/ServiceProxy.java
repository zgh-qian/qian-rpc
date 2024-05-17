package com.qian.qianrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
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
import com.qian.qianrpc.protocol.*;
import com.qian.qianrpc.registry.Registry;
import com.qian.qianrpc.registry.RegistryFactory;
import com.qian.qianrpc.serializer.Serializer;
import com.qian.qianrpc.serializer.SerializerFactory;
import com.qian.qianrpc.server.tcp.VertTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 从注册中心获取服务提供者请求地址
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("暂无服务提供者");
        }
        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        // 将调用方法名（请求路径）作为负载均衡参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        RpcResponse rpcResponse;
        try {
            // 使用重试策略
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                    VertTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );
        } catch (Exception e) {
            // 使用容错
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }
        return rpcResponse.getData();
    }
}
