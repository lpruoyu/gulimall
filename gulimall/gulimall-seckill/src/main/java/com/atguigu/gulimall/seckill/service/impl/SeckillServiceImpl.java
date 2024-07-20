package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSesssionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SeckillSkuVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedissonClient redissonClient;

    private static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private static final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//+商品随机码


    /**
     * 秒杀
     *
     * @param killId 商品id: 2_1 【场次_skuId】
     * @param key    随机码
     * @param num    秒杀的数量
     */
    // TODO 上架秒杀商品的时候，每一个数据都有过期时间。√
    // TODO 秒杀后续的流程
    public String kill(String killId, String key, Integer num) {
        long s1 = System.currentTimeMillis();

        //获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            SecKillSkuRedisTo redis = JSON.parseObject(json, SecKillSkuRedisTo.class);
            //校验合法性
            Long startTime = redis.getStartTime();
            Long endTime = redis.getEndTime();
            long currentTime = new Date().getTime();

            //1、校验时间的合法性
            if (currentTime >= startTime && currentTime <= endTime) {
                //2、校验随机码和商品id
                String randomCode = redis.getRandomCode();
                String promotionSessionId_skuId = redis.getPromotionSessionId() + "_" + redis.getSkuId();
                if (randomCode.equals(key) && killId.equals(promotionSessionId_skuId)) {
                    //3、验证购物数量是否合理
                    if (num <= redis.getSeckillLimit()) {
                        //4、验证这个人是否已经购买过。幂等性; 如果只要秒杀成功，就去占位。  userId_promotionSessionId_skuId
                        //TODO 占位也可以放到一个Redis的Hash里面
                        MemberRespVo userInfo = LoginUserInterceptor.loginUser.get();
                        String redisKey = userInfo.getId() + "_" + promotionSessionId_skuId;
                        //占位得自动过期
                        long ttl = endTime - currentTime;
                        //SETNX
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            //占位成功说明从来没有买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            boolean b = semaphore.tryAcquire(num);
//                            boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                            if (b) {
                                //秒杀成功;
                                //快速下单。发送MQ消息
                                String timeId = IdWorker.getTimeId(); //订单号
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(userInfo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redis.getPromotionSessionId());
                                orderTo.setSkuId(redis.getSkuId());
                                orderTo.setSeckillPrice(redis.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                //TODO  MQ监听消息处理
                                //      用户秒杀成功，信号量不用恢复【商品已经卖出去了】
                                //      用户如果超时未支付或者取消订单，还得去恢复信号量，让其他人能参与秒杀
                                long s2 = System.currentTimeMillis();
                                log.info("耗时...{}", (s2 - s1));

                                return timeId;
                            }
                            return null;
                        } else {
                            //说明已经买过了
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //1、找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId; //6_4
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SecKillSkuRedisTo skuRedisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
                    if (skuRedisTo == null) return null;

                    //随机码
                    long current = new Date().getTime();
                    if (current >= skuRedisTo.getStartTime() && current <= skuRedisTo.getEndTime()) {
//                        正在秒杀中，可以暴露随机码
                        System.out.println("正在秒杀中 getSkuSeckillInfo  skuRedisTo.getEndTime()  " + new Date(skuRedisTo.getEndTime()));
                    } else {
                        System.out.println("不处于秒杀时间 getSkuSeckillInfo  skuRedisTo.getEndTime()  " + new Date(skuRedisTo.getEndTime()));
//                        该商品不处于秒杀时间
//                        当前商品已经过了秒杀时间要从缓存中删除
                        if (current > skuRedisTo.getEndTime()) {
                            hashOps.delete(key);
                            return null;
                        }

                        skuRedisTo.setRandomCode(null);

                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    public List<SecKillSkuRedisTo> blockHandler(BlockException e) {
        log.error("getCurrentSeckillSkusResource被限流了..");
        return null;
    }

    //返回当前时间可以参与的秒杀商品信息

    /**
     * blockHandler 函数会在原方法被限流/降级/系统保护的时候调用，而 fallback 函数会针对所有类型的异常。
     */
    @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "blockHandler"/*, fallback = "getCurrentSeckillSkusFallback"*/)
    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        //1、确定当前时间属于哪个秒杀场次。
        long time = new Date().getTime();

//        try (Entry entry = SphU.entry("resourceName")) {
            Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
            for (String key : keys) {
                //seckill:sessions:1582250400000_1582254000000
                String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                Long start = Long.parseLong(s[0]);
                Long end = Long.parseLong(s[1]);
                if (time >= start && time <= end) {
                    //2、获取这个秒杀场次需要的所有商品信息
                    List<String> range = redisTemplate.opsForList().range(key, -1000, 1000);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    List<String> list = hashOps.multiGet(range);
                    if (list != null) {
                        List<SecKillSkuRedisTo> collect = list.stream().map(item -> {
                            SecKillSkuRedisTo redis = JSON.parseObject((String) item, SecKillSkuRedisTo.class);
//                        redis.setRandomCode(null); 当前秒杀开始就需要随机码
                            return redis;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
//        } catch (BlockException e) {
//            log.error("资源被限流,{}", e.getMessage());
//        }

        return null;
    }

    /**
     * TODO 上架成功后，锁定对应数量的库存，一切都去Redis操作
     *      秒杀结束后，从Redis中根据销售情况，将没卖完的库存加回去
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLates3DaySession();
        if (session.getCode() == 0) {
            //上架商品
            List<SeckillSesssionsWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSesssionsWithSkus>>() {
            });
            //缓存到redis
            //1、缓存活动信息
            saveSessionInfos(sessionData);
            //2、缓存活动的关联商品信息
            saveSessionSkuInfos(sessionData);
        }
    }

    private void saveSessionInfos(List<SeckillSesssionsWithSkus> sesssions) {
        if (sesssions != null) sesssions.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();

            //  如果当前这个场次已经上架就不需要上架
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId()).collect(Collectors.toList());
                //缓存活动信息
                redisTemplate.opsForList().leftPushAll(key, collect);
                //过期时间
                redisTemplate.expireAt(key, new Date(endTime));
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSesssionsWithSkus> sesssions) {
        if (sesssions != null) sesssions.stream().forEach(sesssion -> {
            //准备hash操作
            List<SeckillSkuVo> relationSkus = sesssion.getRelationSkus();
            if (relationSkus != null && relationSkus.size() > 0) {

                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                relationSkus.stream().forEach(seckillSkuVo -> {
                    //4、随机码？  seckill?skuId=1&=dadlajldj
                    final String token = UUID.randomUUID().toString().replace("-", "");

                    //  如果当前这个场次的这个商品的库存信息已经上架就不需要上架
                    final String cacheKey = seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId();
                    if (!ops.hasKey(cacheKey)) {
                        //缓存商品
                        SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                        //1、sku的基本数据
                        R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                        if (skuInfo.getCode() == 0) {
                            SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            redisTo.setSkuInfo(info);
                        }

                        //2、sku的秒杀信息
                        BeanUtils.copyProperties(seckillSkuVo, redisTo);

                        //3、设置上当前商品的秒杀时间信息
                        redisTo.setStartTime(sesssion.getStartTime().getTime());
                        redisTo.setEndTime(sesssion.getEndTime().getTime());

                        redisTo.setRandomCode(token);
                        String jsonString = JSON.toJSONString(redisTo);
                        //每个商品的过期时间不一样。所以，我们在获取当前商品秒杀信息的时候，做主动删除，代码在 getSkuSeckillInfo 方法里面
                        ops.put(cacheKey, jsonString);

                        //TODO 写博客
                        //  使用商品的数量作为分布式的信号量【限流】
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                        //商品可以秒杀的数量作为信号量
                        semaphore.trySetPermits(seckillSkuVo.getSeckillCount());

                        //设置过期时间。
                        semaphore.expireAt(sesssion.getEndTime());
                    }
                });
            }
        });
    }
}
