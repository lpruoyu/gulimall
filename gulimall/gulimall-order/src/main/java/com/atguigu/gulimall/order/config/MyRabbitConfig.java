package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MyRabbitConfig {

    /**
     * 使用JSON序列化机制，进行消息转换
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private RabbitTemplate rabbitTemplate;

    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }

    /**
     * 定制RabbitTemplate
     *      做好消息确认机制（publisher，consumer【手动ack】）
     *      每一个发送的消息都在数据库做好记录。定期将失败的消息再次发送一遍
     *
     * 发送端确认：
     * 1、Broker收到消息就回调【P——>B】
             * 1、spring.rabbitmq.publisher-confirms=true
             * 2、设置确认回调ConfirmCallback
     * 2、消息没有正确抵达队列进行回调【E——>Q】
             * 1、spring.rabbitmq.publisher-returns=true
             *    spring.rabbitmq.template.mandatory=true
             * 2、设置确认回调ReturnCallback
     *
     *
     * 消费端确认（保证每个消息被正确消费，此时broker才可以删除这个消息）。
     *          spring.rabbitmq.listener.simple.acknowledge-mode=manual #手动签收
     *          默认是自动确认的：只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     * 问题：
     *          我们收到很多消息，如果自动回复给服务器ack，当消息由于各种原因没有成功处理完成，就会发生消息丢失；
     * 消费者手动确认模式：
     *          只要我们没有明确告诉MQ消息被签收，消息就一直是unacked状态。
     *          即使Consumer宕机，消息也不会丢失，会重新变为Ready状态，下一次有新的Consumer连接进来就发给他
     * 如何签收:
     *          channel.basicAck(deliveryTag,false);业务成功，签收
     *          channel.basicNack(deliveryTag,false,true);业务失败，拒签，并让消息重新入队；
     */
//    @PostConstruct //MyRabbitConfig对象创建完成以后，也就是构造器执行完成后，执行这个方法
    public void initRabbitTemplate() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {

            /**
             * P ——> B [Publisher ——> Broker]
             *
             * 只要消息抵达Broker这里的ack就为true
             * @param correlationData 当前消息的唯一关联数据（这个是消息的唯一id，发布者发消息的时候传递的）
             * @param ack  消息是否成功抵达Broker
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                //Broker收到了:修改消息的状态——>Broker接收到消息
//                System.out.println("confirm...correlationData[" + correlationData + "]==>ack[" + ack + "]==>cause[" + cause + "]");
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * E ——> Q [Exchange ——> Queue]
             *
             * 只要消息没有正确投递给指定的队列，就会触发这个失败回调
             * @param message   投递失败的消息的详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange  当时这个消息发给了哪个交换机
             * @param routingKey 当时这个消息指定的路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //报错了:修改数据库当前消息的状态——>队列接收错误
//                System.out.println("Fail Message[" + message + "]==>replyCode[" + replyCode + "]==>replyText[" + replyText + "]===>exchange[" + exchange + "]===>routingKey[" + routingKey + "]");
            }
        });
    }

}
