package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2024-05-20 09:42:08
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {
    PageUtils queryPage(Map<String, Object> params);
    void saveSpuInfo(SpuSaveVo vo);

    void saveSpuBaseInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

