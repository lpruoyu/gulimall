package l.p.gulimall.order.dao;

import l.p.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2022-11-27 14:08:32
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
