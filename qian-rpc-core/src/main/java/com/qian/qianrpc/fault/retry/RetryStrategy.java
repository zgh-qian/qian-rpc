package com.qian.qianrpc.fault.retry;


import com.qian.qianrpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试策略
 */
public interface RetryStrategy {

    /**
     * 重试
     *
     * @param callable   待执行的任务
     * @return          任务执行结果
     * @throws Exception     任务执行异常
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
