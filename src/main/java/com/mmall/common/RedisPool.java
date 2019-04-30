package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 描述：Redis连接池
 * 作者：NearJC
 * 时间：2019.4.29
 */
public class RedisPool {

    private static JedisPool pool;

    // IP地址
    private static String redisIp = PropertiesUtil.getProperty("redis1.ip", "127.0.0.1");
    // 端口号
    private static int redisPort = Integer.valueOf(PropertiesUtil.getProperty("redis1.port", "6379"));
    // 最大连接数
    private static int maxTotal = Integer.valueOf(PropertiesUtil.getProperty("redis.max.total", "20"));
    // 最大空闲连接数
    private static int maxIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.max.idle", "10"));
    // 最小空闲连接数
    private static int minIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.min.idle", "2"));
    // 获取验证
    private static boolean testOnBorrow = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    // 回收验证
    private static boolean testOnReturn = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.return", "true"));

    static {
        initPool();
    }

    /**
     * 初始化redis的连接池
     */
    private static void initPool() {
        // 创建连接池参数对象
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setBlockWhenExhausted(true);

        // 创建连接池对象
        pool = new JedisPool(config, redisIp, redisPort, 1000 * 2);
    }

    /**
     * 获取redis操作对象
     *
     * @return
     */
    public static Jedis getResource() {
        return pool.getResource();
    }

    /**
     * 回收正常的jedis
     *
     * @param jedis
     */
    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    /**
     * 回收失效的jedis
     *
     * @param jedis
     */
    public static void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = RedisPool.getResource();
        System.out.println(jedis);
    }
}
