package com.atguigu.gulimall.seckill.scheduled;


import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


/**
 * 秒杀商品的定时上架；
 *     每天晚上3点；上架最近三天需要秒杀的商品。
 *     当天00:00:00  - 23:59:59
 *     明天00:00:00  - 23:59:59
 *     后天00:00:00  - 23:59:59
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private static final String SECKILL_UPLOAD_LOCK = "seckill:upload:lock";

    @Async
    @Scheduled(cron = "*/3 * * * * ?")
//    @Scheduled(cron = "0 * * * * ?") //每分钟执行一次吧，上线后调整为每天晚上3点执行
//    @Scheduled(cron = "0 0 3 * * ?") //线上模式
    public void uploadSeckillSkuLatest3Days(){

        log.info("上架秒杀的商品信息...");

//        seckillService.uploadSeckillSkuLatest3Days();

        //TODO 写博客
        // 分布式锁【集群环境下，让一个服务实例去执行上架即可】
        // 加上分布式锁后，在业务方法里做好判断，即可保证幂等性：
        // 锁的业务执行完成，状态已经更新完成。释放锁以后。其他人获取到锁去执行业务，会拿到最新的状态去判断，不会重复执行
        RLock lock = redissonClient.getLock(SECKILL_UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }

    }

}
