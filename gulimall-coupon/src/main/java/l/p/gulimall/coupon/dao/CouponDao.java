package l.p.gulimall.coupon.dao;

import l.p.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2022-11-27 13:42:10
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
