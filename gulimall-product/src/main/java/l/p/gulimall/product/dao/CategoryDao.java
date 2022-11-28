package l.p.gulimall.product.dao;

import l.p.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2022-11-26 16:08:41
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
