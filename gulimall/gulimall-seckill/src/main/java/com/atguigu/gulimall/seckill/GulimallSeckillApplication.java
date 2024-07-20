package com.atguigu.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * https://sentinelguard.io/zh-cn/docs/quick-start.html
 *
 * 1、整合Sentinel
         1）、导入依赖 spring-cloud-starter-alibaba-sentinel
             <dependency>
                 <groupId>com.alibaba.cloud</groupId>
                 <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
             </dependency>
         2）、下载sentinel的控制台，直接在命令行启动sentinel-dashboard-1.6.3.jar，这是Sentinel的控制台，用户名密码是sentinel
                默认是8080端口，如果该端口被占用，使用java -jar sentinel-dashboard-1.6.3.jar --server.port=xxxx启动
         3）、配置sentinel控制台地址信息
                spring.cloud.sentinel.transport.dashboard=localhost:8080
                spring.cloud.sentinel.transport.port=8719 【默认，可以不用配】
         4) 、在控制台调整参数。【默认所有的流控设置保存在服务内存中，服务重启失效】
         5）、Sentinel默认对SpringCloud进行了适配，每个请求默认就是一个资源

 * 2、每一个微服务都导入spring-boot-starter-actuator ；并配合management.endpoints.web.exposure.include=*
 *      就可以看到Sentinel的实时监控了
 *
 * 3、自定义sentinel流控返回数据UrlBlockHandler
 *
 * 4、使用Sentinel来保护feign远程调用：熔断；
 *    1）、调用方的熔断保护：feign.sentinel.enabled=true
 *    2）、在Sentinel控制台给调用方手动指定远程服务的降级策略。远程服务被降级处理。触发我们的熔断回调方法
 *    3）、超大浏览的时候，必须牺牲一些远程服务。
 *        在服务的提供方（远程服务）指定降级策略；
 *        提供方虽然在运行，但是不运行自己的业务逻辑，返回的是默认的降级数据（限流的数据），
 *
 * 5、自定义受保护的资源 https://sentinelguard.io/zh-cn/docs/basic-api-resource-rule.html
 *   1）、代码
 *    try(Entry entry = SphU.entry("seckillSkus")){
 *        //业务逻辑
 *    }catch(Exception e){}
 *
 *   2）、基于注解。
 *   @SentinelResource(value = "getCurrentSeckillSkusResource",blockHandler = "blockHandler")
        public List<SecKillSkuRedisTo> blockHandler(BlockException e) {
            log.error("getCurrentSeckillSkusResource被限流了..");
            return null;
        }
 *   无论是1,2方式一定要配置好被限流以后的默认返回.
 *   url请求可以设置统一返回:WebCallbackManager  >  UrlBlockHandler
 */
//@EnableRabbit  不监听消息，可以不用这个注解
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
