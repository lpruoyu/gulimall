package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

//@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @RabbitListener(queues = {"hello-java-queue"})
    public void receiveMessage0(Message message,
                                Channel channel) {
//         multiple：是否批量
//         requeue=false 丢弃  requeue=true 发回服务器，让服务器重新入队该消息。

//                          long deliveryTag, boolean multiple
//         channel.basicAck(     deliveryTag,         false); 只签收当前货物，不批量签收；

//                          long deliveryTag, boolean multiple, boolean requeue
//         channel.basicNack(    deliveryTag,         false,            true); 拒签当前货物【是否将该消息让MQ重新放入队列，看自己的业务需求】

//                          long deliveryTag, boolean requeue
//         channel.basicReject(  deliveryTag,         true); 拒签当前货物【是否将该消息重新放回MQ看自己的业务需求】

        //DeliveryTag在channel内按顺序自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag==>" + deliveryTag);

        //签收货物（消息），非批量模式
        try {
            if (deliveryTag % 2 == 0) {
                //收货
                channel.basicAck(deliveryTag, false);
                System.out.println("签收了货物"+ message +"...deliveryTag..." + deliveryTag);
            } else {
                //退货 requeue=false 丢弃  requeue=true 发回服务器，服务器重新入队。

                channel.basicNack(deliveryTag, false, true);
//                channel.basicReject(deliveryTag, true);

//                channel.basicNack(deliveryTag, false, false);

                System.out.println("不签收货物"+ message +"...deliveryTag..." + deliveryTag);

            }
        } catch (Exception e) {
            //网络中断，签收信息未成功发送给Broker
        }

    }

    //    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) throws InterruptedException {
//        接收到消息...(Body:'{"id":1,"name":"哈哈-666","sort":null,"status":null,"createTime":1720438632794}' MessageProperties [headers={__TypeId__=com.atguigu.gulimall.order.entity.OrderReturnReasonEntity}, contentType=application/json, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, redelivered=false, receivedExchange=hello-java-exchange, receivedRoutingKey=hello.java, deliveryTag=1, consumerTag=amq.ctag-ltQ3IQ4H1lvLGqYNyEH3nQ, consumerQueue=hello-java-queue])
        System.out.println("@RabbitListener接收到消息..." + message);
//        System.out.println("接收到消息...content："+content);
//        byte[] body = message.getBody();
//        //消息头属性信息
//        MessageProperties properties = message.getMessageProperties();
//        Thread.sleep(3000);
//        System.out.println("消息处理完成=>"+content.getName());
    }


    //    @RabbitHandler
    public void receiveMessage2(OrderEntity content) throws InterruptedException {
        //{"id":1,"name":"哈哈","sort":null,"status":null,"createTime":1581144531744}
        System.out.println("@RabbitHandler接收到消息..." + content);
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

}