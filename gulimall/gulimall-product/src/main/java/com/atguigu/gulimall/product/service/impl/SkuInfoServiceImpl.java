package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), new QueryWrapper<SkuInfoEntity>());

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /*
            key:
            catelogId: 0
            brandId: 0
            min: 0
            max: 0
         */

        Object key = params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
//        queryWrapper.gt("column",val); great than 【大于】
//        queryWrapper.lt("column",val); less than 【小于】
//        queryWrapper.ge("column",val); great than and equal 【大于等于】
//        queryWrapper.le("column",val); less than and equal 【小于等于】
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }
//  BigDecimal使用compareTo比较大小：
//        a.compareTo(b) == 1  【a > b】
//        a.compareTo(b) == 0  【a = b】
//        a.compareTo(b) == -1 【a < b】
//        引申：
//        a.compareTo(b) > -1 【a >= b】
//        a.compareTo(b) < 1  【a <= b】
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            BigDecimal maxVal = new BigDecimal(max);
            if (!(maxVal.compareTo(new BigDecimal(0)) < 1)) {
                queryWrapper.le("price", max);
            }
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> listSkusBySpuId(Long spuId) {
        return list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

}