package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;

//@FeignClient("gulimall-coupon")
public interface CouponFeignService {

//    @RequestMapping("coupon/coupon/couponInfo")
    //都可以
    @RequestMapping("/coupon/coupon/couponInfo")
    public R couponInfo();

}
