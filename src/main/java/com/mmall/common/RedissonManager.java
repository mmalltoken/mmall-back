package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 描述：Redisson管理者类
 * 作者：NearJC
 * 时间：2019.5.1
 */
@Component
@Slf4j
public class RedissonManager {

    private Config config = new Config();
    private Redisson redisson;

    private String redisIp = PropertiesUtil.getProperty("redis1.ip", "127.0.0.1");
    private Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis1.port", "6379"));

    @PostConstruct
    private void init() {
        try {
            config.useSingleServer().setAddress(new StringBuilder().append(redisIp).append(":").append(redisPort).toString());
            redisson = (Redisson) Redisson.create(config);
            log.info("Redisson初始化结束");
        } catch (Exception e) {
            log.error("Redisson初始化异常", e);
        }
    }

    public Redisson getRedisson() {
        return redisson;
    }
}
