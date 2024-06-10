package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), new QueryWrapper<AttrEntity>());

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attrvo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrvo, attrEntity);
        this.save(attrEntity);

        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity =
                new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
        attrAttrgroupRelationEntity.setAttrGroupId(attrvo.getAttrGroupId());
        attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
    }

    @Override
    public PageUtils queryAttrBasePage(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();

        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        Object key = params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                queryWrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(page);

     /*
     需要多返回：
    	"catelogName": "手机/数码/手机", //所属分类名字
		"groupName": "主体", //所属分组名字
     */
        List<AttrRespVo> attrRespVos = page.getRecords().stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao
                    .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", attrEntity.getAttrId()));
            if (relationEntity != null) {
                Long attrGroupId = relationEntity.getAttrGroupId();
                if (null != attrGroupId) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                    if (null != attrGroupEntity) attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectOne(
                    new QueryWrapper<CategoryEntity>()
                            .eq("cat_id", attrEntity.getCatelogId()));
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(attrRespVos);

        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attrEntity = this.baseMapper.selectOne(new QueryWrapper<AttrEntity>()
                .eq("attr_id", attrId));
        BeanUtils.copyProperties(attrEntity, respVo);

        AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao
                .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_id", attrId));
        if (relationEntity != null) {
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
            if (null != attrGroupEntity) {
                respVo.setAttrGroupId(attrGroupEntity.getAttrGroupId());
                respVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        CategoryEntity categoryEntity = categoryDao.selectOne(
                new QueryWrapper<CategoryEntity>()
                        .eq("cat_id", attrEntity.getCatelogId()));
        if (categoryEntity != null) {
            respVo.setCatelogName(categoryEntity.getName());
        }
        respVo.setCatelogPath(categoryService.findCatelogPath(attrEntity.getCatelogId()));
        return respVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrvo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrvo, attrEntity);
        this.updateById(attrEntity);

        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrId(attrvo.getAttrId());
        relationEntity.setAttrGroupId(attrvo.getAttrGroupId());

        //有可能属性分组和属性并没有关联
        Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrvo.getAttrId()));
        if (count > 0) { // 该属性有关联的分组
            attrAttrgroupRelationDao.update(relationEntity,
                    new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrvo.getAttrId()));
        } else {
            attrAttrgroupRelationDao.insert(relationEntity);
        }
    }

}