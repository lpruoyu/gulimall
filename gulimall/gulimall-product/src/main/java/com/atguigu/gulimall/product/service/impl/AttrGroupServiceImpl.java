package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), new QueryWrapper<AttrGroupEntity>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> queryPage = new Query<AttrGroupEntity>().getPage(params);
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        /*
        该请求这么查询：
        select * from pms_attr_group
        where catelog_id = catelogId and (attr_group_id = key or attr_group_name like '%key%')
         */

        if (catelogId != 0) { // 规定在分页查询下，id为0，代表查询所有
            wrapper.eq("catelog_id", catelogId);
        }

        Object key = params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        return new PageUtils(this.page(queryPage, wrapper));
    }

    @Override
    public List<AttrGroupWithAttrsVo> queryAttrGroupsWithAttrsByCatelogId(Long catelogId) {
//        根据catelogId查询出所有的分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        if (null != attrGroupEntities && attrGroupEntities.size() > 0) {
            List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(group -> {
                AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
                BeanUtils.copyProperties(group, vo);

//            查询每一个分组的所有属性
                List<AttrEntity> attrEntities = attrService.queryAttrsByAttrgroupId(group.getAttrGroupId());
                if (attrEntities != null && attrEntities.size() > 0)
                    vo.setAttrs(attrEntities);

                return vo;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

}