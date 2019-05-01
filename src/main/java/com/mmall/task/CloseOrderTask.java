package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 描述：定时关闭未支付的订单
 * 作者：NearJC
 * 时间：2019.5.1
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private RedissonManager redissonManager;
    @Autowired
    private IOrderService orderService;

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTask() {
        log.info("定时关闭订单任务启动...");
        RLock lock = redissonManager.getRedisson().getLock(Const.RedisCache.CLOSE_ORDER_LOCK);
        boolean locked = false;

        try {
            if (locked = lock.tryLock(0, 5, TimeUnit.SECONDS)) {
                int hour = Integer.parseInt(PropertiesUtil.getProperty("redis.close.order.hour", "2"));
                orderService.closeOrder(hour);
            } else {
                log.info("没有获取锁：{}", Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("定时关闭订单任务异常", e);
        } finally {
            if (!locked) {
                return;
            }
            lock.unlock();
            log.info("分布式锁释放");
        }

        log.info("定时关闭订单任务结束...");
    }
}
