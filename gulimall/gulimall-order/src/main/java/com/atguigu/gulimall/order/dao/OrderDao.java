package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2024-05-22 14:21:06
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}