package com.qian.qianrpc.registry;

import com.qian.qianrpc.config.RegistryConfig;
import com.qian.qianrpc.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ZooKeeperRegistry implements Registry {
    @Override
    public void init(RegistryConfig registryConfig) {

    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {

    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {

    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        return null;
    }

    @Override
    public void heartBeat() {

    }

    @Override
    public void watch(String serviceNodeKey) {

    }

    @Override
    public void destroy() {

    }
}
