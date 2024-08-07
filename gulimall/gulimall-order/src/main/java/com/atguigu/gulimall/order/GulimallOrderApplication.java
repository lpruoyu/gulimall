package com.atguigu.gulimall.order;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 使用RabbitMQ
 * 1、引入amqp场景启动器；RabbitAutoConfiguration 就会自动生效
 *
 * 2、给容器中自动配置了
 *      RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate;
 *      所有的属性都是 spring.rabbitmq
 *      @ConfigurationProperties(prefix = "spring.rabbitmq")
 *      public class RabbitProperties
 *
 * 3、给配置文件中配置 spring.rabbitmq 信息
 *
 * 4、@EnableRabbit: @EnableXxxxx；开启功能
 *
 * 5、监听消息：使用@RabbitListener；必须有@EnableRabbit
 *    @RabbitListener: 类+方法上（监听哪些队列即可）
 *    @RabbitHandler：配合@RabbitListener标在方法上（重载区分不同的消息）
 *
 *
 * 本地事务方法互调，其他方法的事务设置失效问题
 * 同一个对象内，事务方法互调，其他方法的事务设置默认失效，原因：绕过了代理对象，事务使用代理对象来控制的
 * 解决：使用代理对象来调用其他事务方法
 *   1）、引入aop-starter：spring-boot-starter-aop；引入了aspectj
 *   2）、@EnableAspectJAutoProxy(exposeProxy = true)；开启 aspectj 动态代理功能。
 *       以后所有的动态代理都是aspectj创建的（即使没有接口也可以创建动态代理）。
 *       exposeProxy = true：对外暴露代理对象
 *   3）、本类互调用代理对象调
 *      OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
 *          orderService.b();
 *          orderService.c();
 *
 *  Seata控制分布式事务
 *  1）、每一个微服务先必须创建 undo_log 表；
 *  2）、安装事务协调器；seata-server： https://github.com/seata/seata/releases
 *  3）、整合
 *      1、导入依赖 spring-cloud-starter-alibaba-seata ；seata-all-0.7.1，所以该项目使用seata-server-0.7.1.zip
                    <dependency>
                        <groupId>com.alibaba.cloud</groupId>
                        <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
                    </dependency>
 *      2、解压并启动seata-server；
 *          修改注册中心配置registry.conf:
                        registry {
                            type = "nacos"

                            nacos {
                                serverAddr = "localhost:8848"
                                ...
                            }
                        }
 *      3、所有想要用到分布式事务的微服务使用seata的DataSourceProxy代理自己的数据源
 *      4、每个微服务，都必须把seata-server-0.7.1/conf目录下的registry.conf、file.conf这两个文件拷贝到自己的resources目录下
 *                  然后修改自己微服务下的file.conf：
 *                      vgroup_mapping.{application.name}-fescar-service-group = "default"
 *                  比如订单服务：
 *                      vgroup_mapping.gulimall-order-fescar-service-group = "default"
 *      5、给分布式大事务的入口标注@GlobalTransactional
 *      6、每一个远程的小事务用 @Transactional
 *      7、启动测试分布式事务
 */

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
@EnableFeignClients
@EnableRedisHttpSession
@EnableRabbit
@MapperScan("com.atguigu.gulimall.order.dao")
@EnableDiscoveryClient
@SpringBootApplication(exclude = GlobalTransactionAutoConfiguration.class)
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
