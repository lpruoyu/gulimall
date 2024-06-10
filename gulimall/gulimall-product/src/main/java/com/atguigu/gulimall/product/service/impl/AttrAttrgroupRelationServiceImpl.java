package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.vo.AttrAttrGroupRelationVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    //    @Transactional
//    @Override
//    public void batchDeleteRelation(AttrAttrGroupRelationVo[] vos) {
//        List<AttrAttrgroupRelationEntity> collect = Arrays.stream(vos).map(item -> {
//            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
//            BeanUtils.copyProperties(item, relationEntity);
//            return relationEntity;
//        }).collect(Collectors.toList());
////        this.baseMapper.batchDeleteRelation(collect);
//    }

    @Transactional
    @Override
    public void batchDeleteRelation(List<AttrAttrGroupRelationVo> relationVos) {
        this.baseMapper.batchDeleteRelation(relationVos);
    }

}