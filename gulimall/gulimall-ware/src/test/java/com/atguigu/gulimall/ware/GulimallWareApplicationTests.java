package com.atguigu.gulimall.ware;

import com.atguigu.gulimall.ware.service.WareInfoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallWareApplicationTests {

    @Autowired
    WareInfoService service;

    @Test
    public  void contextLoads() {
//        WareInfoEntity entity = new WareInfoEntity();
//        entity.setAddress("address111");
//        entity.setId(1L);
//        entity.setAreacode("areacode");
//        entity.setName("水水水水");
//        service.save(entity);
    }

}
