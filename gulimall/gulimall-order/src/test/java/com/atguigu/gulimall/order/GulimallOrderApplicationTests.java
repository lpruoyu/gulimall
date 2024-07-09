package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    public AmqpAdmin amqpAdmin;

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest() {

        //1、发送消息，如果发送的消息是个对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable
        String msg = "Hello World!";
//        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", msg);

//      2、发送的对象类型的消息，可以自动转成一个json【自定义MessageConverter即可】
//        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
//        reasonEntity.setId(1L);
//        reasonEntity.setCreateTime(new Date());
//        reasonEntity.setName("哈哈-"+ 666);
//        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java",
//                reasonEntity);

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("哈哈-" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
            } else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity);
            }
        }

        log.info("消息发送完成{}");

    }

    /**
     * 1、使用 AmqpAdmin 进行创建 Exchange[hello-java-exchange]、Queue、Binding
     * 2、如何收发消息
     */

    @Test
    public void createExchange() {
        /**
         * DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
         */
        Exchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }

    @Test
    public void createQueue() {
        /**
         * public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
         */
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    @Test
    public void createBinding() {
        /**
         * String destination【目的地】,
         * DestinationType destinationType【目的地类型】,
         * String exchange【交换机】,
         * String routingKey【路由键】,
         * Map<String, Object> arguments【自定义参数】
         *
         * 将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为指定的路由键
         */
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE, "hello-java-exchange", "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-binding");
    }

}
