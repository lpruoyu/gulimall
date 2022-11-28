package l.p.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import l.p.common.utils.PageUtils;
import l.p.gulimall.product.entity.AttrEntity;

import java.util.Map;

/**
 * 商品属性
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2022-11-26 16:08:41
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

