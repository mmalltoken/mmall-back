package com.mmall.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 描述：本地缓存工具类
 * 作者：NearJC
 * 时间：2019.4.24
 */
@Slf4j
public class TokenCache {

    public static final String TOKEN_PREFIX = "token_";    // 前缀
    private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder().
            initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS).
            build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    /**
     * 存储数据
     *
     * @param key
     * @param value
     */
    public static void set(String key, String value) {
        localCache.put(key, value);
    }

    /**
     * 获取本地数据
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        String value = null;

        try {
            value = localCache.get(key);
            if ("null".equals(value)) {
                return null;
            }
        } catch (ExecutionException e) {
            log.error("获取本地数据异常", e);
        }

        return value;
    }

}
