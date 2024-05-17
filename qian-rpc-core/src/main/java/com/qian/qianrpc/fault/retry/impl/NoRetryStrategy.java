package com.qian.qianrpc.fault.retry.impl;

import com.qian.qianrpc.fault.retry.RetryStrategy;
import com.qian.qianrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * 不重试 -  重试策略
 */
@Slf4j
public class NoRetryStrategy implements RetryStrategy {

    /**
     * 不重试，仅执行一次
     *
     * @param callable 待执行的任务
     * @return 任务执行结果
     * @throws Exception 任务执行异常
     */
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
