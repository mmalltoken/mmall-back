package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 描述：定时关闭未支付订单
 * 作者：NearJC
 * 时间：2019.5.1
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService orderService;

    /**
     * 每一分钟扫描一次未支付的订单进行关闭
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTask() {
        log.info("定时关闭订单任务启动");

        Long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        // 获取分布式锁
        Long lockedResult = RedisShardedUtil.setnx(Const.RedisCache.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeout));
        // 结果不为空且等于1时获取执行权限
        if (lockedResult != null && lockedResult == 1) {
            // 关闭订单
            closeOrder(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
        } else {
            String lockValue = RedisShardedUtil.get(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
            if (lockValue != null && System.currentTimeMillis() > Long.parseLong(lockValue)) {
                // 重新设置锁
                String getSetResult = RedisShardedUtil.getSet(Const.RedisCache.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(getSetResult, lockValue))) {
                    // 关闭订单
                    closeOrder(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("没有获取分布式锁");
                }
            } else {
                log.info("没有获得分布式锁");
            }
        }

        log.info("定时关闭订单任务结束");
    }

    private void closeOrder(String lockName) {
        // 设置有效时间
        RedisShardedUtil.expire(lockName, 5);  // 有效期为5秒，防止死锁
        log.info("锁名称：{},线程：{}", lockName, Thread.currentThread().getName());

        // 关闭订单
        int hour = Integer.valueOf(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        orderService.closeOrder(hour);

        RedisShardedUtil.del(lockName);
        log.info("释放锁：{}", Thread.currentThread().getName());
        log.info("==============================================");
    }
}
