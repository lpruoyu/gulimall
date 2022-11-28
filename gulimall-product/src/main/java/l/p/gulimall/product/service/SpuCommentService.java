package l.p.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import l.p.common.utils.PageUtils;
import l.p.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2022-11-26 16:08:41
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

