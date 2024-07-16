package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GulimallWareRabbitBaseConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

//    TODO 为了保证消息可靠发送，去抽取一个MQ微服务，发送MQ消息，都调用该微服务的接口
//     在MQ微服务里每次发送消息前将该消息保存到数据库，并做好发送回调，修改消息的发送状态
//     定时扫描数据库，再次发送那些发送失败的消息

//    private RabbitTemplate rabbitTemplate;
//
//    @Primary
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        this.rabbitTemplate = rabbitTemplate;
//        rabbitTemplate.setMessageConverter(messageConverter());
//        initRabbitTemplate();
//        return rabbitTemplate;
//    }
//
//    public void initRabbitTemplate() {
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//            }
//        });
//
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            @Override
//            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//            }
//        });
//    }

}
