package com.qian.qianrpc.fault.retry;


import com.qian.qianrpc.fault.retry.RetryStrategy;
import com.qian.qianrpc.fault.retry.impl.NoRetryStrategy;
import com.qian.qianrpc.spi.SpiLoader;

/**
 * 重试策略工厂（用于获取重试器对象）
 */
public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试器
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * 获取实例
     *
     * @param key    策略名称
     * @return    重试器对象
     */
    public static RetryStrategy getInstance(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }

}
