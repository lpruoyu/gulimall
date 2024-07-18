package com.atguigu.gulimall.seckill;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GulimallSeckillApplicationTests {

    //TODO 写博客 Java8的日期API
    @Test
    public void contextLoads() {
        LocalDate now = LocalDate.now();
        LocalDate dayAfter2 = now.plusDays(2);
        System.out.println("now : " + now);
        System.out.println("两天后 : " + dayAfter2);

        LocalTime min = LocalTime.MIN;
        LocalTime max = LocalTime.MAX;
        System.out.println("最小时间：" + min);
        System.out.println("最大时间：" + max);

//        最近三天开始的活动：2024-07-17 00:00:00  到  2024-07-19 23:59:59
        LocalDateTime start = LocalDateTime.of(now, min);
        LocalDateTime end = LocalDateTime.of(dayAfter2, max);
        System.out.println("开始时间：" + start);
        System.out.println("结束时间：" + end);
        System.out.println("结束时间：" + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("结束时间：" + end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}
