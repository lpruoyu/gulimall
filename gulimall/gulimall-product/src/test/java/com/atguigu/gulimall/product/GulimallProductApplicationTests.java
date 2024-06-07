package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

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
