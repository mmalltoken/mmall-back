package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：redis分布式连接池
 * 作者：NearJC
 * 时间：2019.4.30
 */
public class RedisShardedPool {

    private static ShardedJedisPool shardedJedisPool;

    // redis1的ip地址
    private static String redisIp1 = PropertiesUtil.getProperty("redis1.ip", "127.0.0.1");
    // redis1的端口号
    private static int redisPort1 = Integer.parseInt(PropertiesUtil.getProperty("redis1.port", "6379"));
    // redis2的ip地址
    private static String redisIp2 = PropertiesUtil.getProperty("redis2.ip", "127.0.0.1");
    // redis2的端口号
    private static int redisPort2 = Integer.parseInt(PropertiesUtil.getProperty("redis2.port", "6380"));

    // 最大连接数
    private static int maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    // 最大空闲连接数
    private static int maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));
    // 最小空闲连接数
    private static int minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));
    // 获取验证
    private static boolean testOnBorrow = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    // 回收验证
    private static boolean testOnReturn = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.return", "true"));

    static {
        initPool();
    }

    /**
     * 初始化连接池
     */
    private static void initPool() {
        // 设置连接池配置信息
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        // 添加连接节点
        JedisShardInfo shardInfo1 = new JedisShardInfo(redisIp1, redisPort1);
        JedisShardInfo shardInfo2 = new JedisShardInfo(redisIp2, redisPort2);

        List<JedisShardInfo> shardInfoList = new ArrayList<>(2);
        shardInfoList.add(shardInfo1);
        shardInfoList.add(shardInfo2);

        // 创建分布式连接池对象
        shardedJedisPool = new ShardedJedisPool(config, shardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    /**
     * 获取连接
     *
     * @return
     */
    public static ShardedJedis getResource() {
        return shardedJedisPool.getResource();
    }

    /**
     * 回收正常的连接
     *
     * @param shardedJedis
     */
    public static void returnResource(ShardedJedis shardedJedis) {
        shardedJedisPool.returnResource(shardedJedis);
    }

    /**
     * 回收异常的连接
     *
     * @param shardedJedis
     */
    public static void returnBrokenResource(ShardedJedis shardedJedis) {
        shardedJedisPool.returnBrokenResource(shardedJedis);
    }

}
