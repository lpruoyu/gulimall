package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
//@FeignClient("gulimall-gateway") //让请求过网关
public interface ProductFeignService {
    @RequestMapping("product/skuinfo/info/{skuId}")
//    @RequestMapping("api/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
