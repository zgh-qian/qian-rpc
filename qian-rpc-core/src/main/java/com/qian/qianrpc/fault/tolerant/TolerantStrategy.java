package com.qian.qianrpc.fault.tolerant;


import com.qian.qianrpc.model.RpcResponse;

import java.util.Map;

/**
 * 容错策略
 */
public interface TolerantStrategy {

    /**
     * 容错
     *
     * @param context 上下文，用于传递数据
     * @param e       异常
     * @return 容错后的RpcResponse
     */
    RpcResponse doTolerant(Map<String, Object> context, Exception e);
}
