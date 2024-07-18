package com.atguigu.gulimall.seckill.to;

import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class SecKillSkuRedisTo {

    /**
     * 活动id
     */
    private Long promotionId;

    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    /**
     *    商品秒杀随机码：
             没有随机码的话，秒杀接口假如是这样：seckill?skuId=1，这样不安全容易引起脚本攻击秒杀
             有了随机码：seckill?skuId=1&key=sfuhgregsfds2fdsf4 ，商品开始秒杀这个随机码才会暴露出来；就算你知道哪个商品要秒杀没有随机码你也秒杀不了
          随机码还可以用于当作该商品的分布式信号量，来限流
     */
    private String randomCode;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;
    //当前商品秒杀的开始时间
    private Long startTime;

    //当前商品秒杀的结束时间
    private Long endTime;

    //sku的详细信息
    private SkuInfoVo skuInfo;

}
