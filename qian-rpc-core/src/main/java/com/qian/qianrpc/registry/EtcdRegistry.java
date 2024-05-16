package com.qian.qianrpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.qian.qianrpc.config.RegistryConfig;
import com.qian.qianrpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry {
    private Client client;

    private KV kvClient;

    /**
     * etcd的根路径
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();


    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 初始化注册中心
     *
     * @param registryConfig 注册中心配置
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        heartBeat();
    }

    /**
     * 注册服务
     *
     * @param serviceMetaInfo 服务元信息
     * @throws Exception 注册异常
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建 lease 客户端
        Lease leaseClient = client.getLeaseClient();
        // 申请租约 30s
        long leaseId = leaseClient.grant(30).get().getID();
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        // 设置 KV 值
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
        // 将 KV 值和租约绑定
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();
        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    /**
     * 注销服务
     *
     * @param serviceMetaInfo 服务元信息
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));
        // 移除本地缓存的节点信息
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * 服务发现
     *
     * @param serviceKey 服务键名
     * @return 服务元信息列表
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从缓存获取服务
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if (cachedServiceMetaInfoList != null) {
            return cachedServiceMetaInfoList;
        }
        // 前缀搜索，结尾一定要加 '/'
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";
        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption).get().getKvs();
            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        // 监听 key 的变化
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());
            // 写入服务缓存
            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    /**
     * 心跳检测，续约租约
     */
    @Override
    public void heartBeat() {
        // 10s 一次心跳
        CronUtil.schedule("*/10 * * * * *", new Task() {

            @Override
            public void execute() {
                // 遍历本地缓存的节点信息，续约租约
                for (String registerKey : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValueList = kvClient.get(ByteSequence.from(registerKey, StandardCharsets.UTF_8)).get().getKvs();
                        // 该节点过期
                        if (CollUtil.isEmpty(keyValueList)) {
                            continue;
                        }
                        // 节点未过期，续约租约
                        KeyValue keyValue = keyValueList.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                        //System.out.println(registerKey + " 续约租约成功");
                    } catch (Exception e) {
                        throw new RuntimeException(registerKey + " 续约租约失败:" + e);
                    }
                }
            }
        });
        // 启动定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 监听服务变化
     *
     * @param serviceNodeKey 服务节点键名
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()) {
                        // key 删除时触发
                        case DELETE:
                            // 清理注册服务缓存
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }

    /**
     * 释放资源
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        // 下线节点，遍历本节点所有的 key
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }
        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 测试 etcd 连接
        Client client = Client.builder().endpoints("http://localhost:2379").build();
        KV kvClient = client.getKVClient();

        ByteSequence key = ByteSequence.from("test_key".getBytes());
        ByteSequence value = ByteSequence.from("test_value".getBytes());

        kvClient.put(key, value);

        CompletableFuture<GetResponse> getResponseCompletableFuture = kvClient.get(key);
        GetResponse response = getResponseCompletableFuture.get();

        kvClient.delete(key).get();
    }
}
