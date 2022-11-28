package l.p.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import l.p.common.utils.PageUtils;
import l.p.gulimall.product.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu属性值
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2022-11-26 16:08:41
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

