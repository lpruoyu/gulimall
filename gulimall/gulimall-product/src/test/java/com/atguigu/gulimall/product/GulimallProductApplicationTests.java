package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedissonClient(){
        System.out.println(redissonClient);
    }


    //==========================

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testStringRedisTemplate(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        //保存
        //hello   world
        ops.set("hello","world_"+ UUID.randomUUID());

        //查询
        String hello = ops.get("hello");
        System.out.println("之前保存的数据是："+hello);
    }


//    ========================================

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Test
    public void testCategoryPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：" + Arrays.asList(catelogPath));
    }

    @Test
    public void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("aaa");
//        brandEntity.setDescript("aaaaaa");
//        brandEntity.setLogo("ssssssssssss");
//        brandService.save(brandEntity);

        brandService.list().stream().forEach(c -> System.out.println(c.toString()));

//        brandService.removeByIds(Arrays.asList(1, 2));
    }

}
