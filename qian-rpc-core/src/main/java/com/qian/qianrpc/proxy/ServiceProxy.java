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
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
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
            // HTTP 请求
            /*
            // 指定序列化器
            final Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }*/
            // TCP 请求
            /*Vertx vertx = Vertx.vertx();
            NetClient netClient = vertx.createNetClient();
            CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
            netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getServiceAddress(), result -> {
                if (result.succeeded()) {
                    System.out.println("Connected to TCP server");
                    NetSocket socket = result.result();
                    // 发送数据
                    ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                    ProtocolMessage.Header header = new ProtocolMessage.Header();
                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                    header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                    header.setRequestId(IdUtil.getSnowflakeNextId());
                    protocolMessage.setHeader(header);
                    protocolMessage.setBody(rpcRequest);
                    // 编码请求
                    try {
                        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                        socket.write(encodeBuffer);
                    } catch (IOException e) {
                        throw new RuntimeException("协议编码失败:" + e);
                    }
                    // 接收响应
                    socket.handler(buffer -> {
                        try {
                            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                            responseFuture.complete(rpcResponseProtocolMessage.getBody());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    System.out.println("Failed to connect to TCP server");
                }
            });
            // 阻塞等待响应
            RpcResponse rpcResponse = responseFuture.get();
            netClient.close();
            return rpcResponse.getData();*/
            // 重试
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            RpcResponse rpcResponse = retryStrategy.doRetry(() ->
                    VertTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );
            return rpcResponse.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
