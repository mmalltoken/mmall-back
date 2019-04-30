package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * 描述：redis操作工具类
 * 作者：NearJC
 * 时间：2019.4.29
 */
@Slf4j
public class RedisUtil {

    /**
     * 设置有效时间
     *
     * @param key
     * @param exTime
     */
    public static Long expire(String key, int exTime) {
        Jedis jedis = null;
        Long result = 0L;

        try {
            jedis = RedisPool.getResource();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            log.error("设置有效时间异常", e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);

        return result;
    }

    /**
     * 设置属性并设置有效时间
     *
     * @param key
     * @param value
     * @param exTime
     * @return
     */
    public static String setex(String key, String value, int exTime) {
        Jedis jedis = null;
        String result = "";

        try {
            jedis = RedisPool.getResource();
            result = jedis.setex(key, exTime, value);
        } catch (Exception e) {
            log.error("设置属性和有效时间异常", e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);

        return result;
    }

    /**
     * 设置属性
     *
     * @param key
     * @param value
     * @return
     */
    public static String set(String key, String value) {
        Jedis jedis = null;
        String result = "";

        try {
            jedis = RedisPool.getResource();
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("设置属性异常", e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);

        return result;
    }

    /**
     * 获取属性值
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        Jedis jedis = null;
        String result = "";

        try {
            jedis = RedisPool.getResource();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("设置属性异常", e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);

        return result;
    }

    public static Long del(String key) {
        Jedis jedis = null;
        Long result = 0L;

        try {
            jedis = RedisPool.getResource();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("删除属性异常", e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);

        return result;
    }

    public static void main(String[] args) {
        RedisUtil.set("a", "a");
        RedisUtil.set("b", "b");

        String a = RedisUtil.get("a");
        System.out.println("a = " + a);

        RedisUtil.del("b");
    }
}
