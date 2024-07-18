package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务
 *      1、@EnableScheduling 开启定时任务
 *      2、@Scheduled  开启一个定时任务
 *      3、自动配置类 TaskSchedulingAutoConfiguration
 *
 * 异步任务
 *      1、@EnableAsync 开启异步任务功能
 *      2、@Async 给希望异步执行的方法上标注
 *      3、自动配置类 TaskExecutionAutoConfiguration
 */
@Slf4j
//@Component
//@EnableAsync
//@EnableScheduling
public class HelloSchedule {

    /**
     * Spring的Scheduled与quartz的区别：
           1、Spring中6位组成：秒    分    时    日    月    周，不允许第7位的年
           2、在周几的位置，Spring的1-7代表周一到周日； MON-SUN
           3、定时任务不应该阻塞（一个定时任务阻塞了不应该影响其它的定时任务按时执行）。默认是阻塞的
                1）、让业务以异步的方式运行，自己提交到线程池
                         CompletableFuture.runAsync(()->{
                             xxxxService.hello();
                         },executor);
                2）、Spring支持定时任务线程池【不太好使】；TaskSchedulingAutoConfiguration ——> TaskSchedulingProperties；
                         spring.task.scheduling.pool.size=5
                3）、让定时任务异步执行
                    异步任务：@EnableAsync 开启异步任务功能，给希望异步执行的方法上标注 @Async
                         spring.task.execution.pool.core-size=5
                         spring.task.execution.pool.max-size=50
                         #spring.task.execution.pool.allow-core-thread-timeout=true
                         spring.task.execution.pool.queue-capacity=100
               4）、总结：使用异步+定时任务来完成定时任务不阻塞的功能；
     */
//                    秒 分时日 月周
//    @Scheduled(cron = "*/5 * * ? * 3")
    @Scheduled(cron = "* * * ? * 3")
    @Async
    public void hello() throws InterruptedException {
        log.info("hello...");
        Thread.sleep(3000);
    }

}
