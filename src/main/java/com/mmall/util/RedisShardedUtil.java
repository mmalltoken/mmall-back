package com.mmall.util;

import com.mmall.common.Const;
import com.mmall.common.RedisShardedPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ShardedJedis;

/**
 * 描述：redis分布式操作工具类
 * 作者：NearJC
 * 时间：2019.4.30
 */
@Slf4j
public class RedisShardedUtil {

    /**
     * 设置有效时间
     *
     * @param key
     * @param exTime
     * @return
     */
    public static Long expire(String key, int exTime) {
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.expire(key, exTime);
            RedisShardedPool.returnResource(jedis);
        } catch (Exception e) {
            log.error("redis设置有效时间异常", e);
            RedisShardedPool.returnBrokenResource(jedis);
        }

        return result;
    }

    /**
     * 存储数据并设置有效时间
     *
     * @param key
     * @param value
     * @param exTime
     * @return
     */
    public static String setex(String key, String value, int exTime) {
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.setex(key, exTime, value);
            RedisShardedPool.returnResource(jedis);
        } catch (Exception e) {
            log.error("存储数据并设置有效时间异常", e);
            RedisShardedPool.returnBrokenResource(jedis);
        }

        return result;
    }

    /**
     * 存储数据
     *
     * @param key
     * @param value
     * @return
     */
    public static String set(String key, String value) {
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.set(key, value);
            RedisShardedPool.returnResource(jedis);
        } catch (Exception e) {
            log.error("存储数据异常", e);
            RedisShardedPool.returnBrokenResource(jedis);
        }

        return result;
    }

    /**
     * 获取数据
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        ShardedJedis jedis = null;
        String value = null;

        try {
            jedis = RedisShardedPool.getResource();
            value = jedis.get(key);
            RedisShardedPool.returnResource(jedis);
        } catch (Exception e) {
            log.error("存储数据异常", e);
            RedisShardedPool.returnBrokenResource(jedis);
        }

        return value;
    }

    /**
     * 删除数据
     *
     * @param key
     * @return
     */
    public static Long del(String key) {
        ShardedJedis jedis = null;
        Long value = null;

        try {
            jedis = RedisShardedPool.getResource();
            value = jedis.del(key);
            RedisShardedPool.returnResource(jedis);
        } catch (Exception e) {
            log.error("删除数据异常", e);
            RedisShardedPool.returnBrokenResource(jedis);
        }

        return value;
    }

    /**
     * 没有数据时才设置
     *
     * @param key
     * @param value
     * @return
     */
    public static Long setnx(String key, String value) {
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.setnx(key, value);
            RedisShardedPool.returnResource(jedis);
        } catch (Exception e) {
            log.error("删除数据异常", e);
            RedisShardedPool.returnBrokenResource(jedis);
        }

        return result;
    }

    /**
     * 设置新值数据并返回旧数据
     *
     * @param key
     * @param value
     * @return
     */
    public static String getSet(String key, String value) {
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.getSet(key, value);
            RedisShardedPool.returnResource(jedis);
        } catch (Exception e) {
            log.error("删除数据异常", e);
            RedisShardedPool.returnBrokenResource(jedis);
        }

        return result;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            RedisShardedUtil.setex("key" + i, "key" + i, Const.RedisCache.REDIS_SESSION_EXPIRE);
        }
    }
}
