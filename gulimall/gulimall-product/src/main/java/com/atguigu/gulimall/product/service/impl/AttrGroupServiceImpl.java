package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), new QueryWrapper<AttrGroupEntity>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> page;
        IPage<AttrGroupEntity> queryPage = new Query<AttrGroupEntity>().getPage(params);
        if (catelogId == 0) { // 规定在分页查询下，id为0，代表查询所有
            page = this.page(queryPage, new QueryWrapper<>());
        } else {
            /*
            分页查询：
                # 假设每页15条（pageSize = 15）
                # 假设查询第n页 （n >= 1）
                #   SELECT * FROM student LIMIT (n - 1) * pageSize, pageSize;
                    SELECT * FROM student LIMIT 0, 15; # 查询第一页
                    SELECT * FROM student LIMIT 15, 15; # 查询第二页

            总数量：101条
            每一页显示20条
            公式：总页数 = (总数量  +  每页的数量   -   1) / 每页的数量
                        = ( 101   +    20        -   1) / 20
             */
            /*
            该请求这么查询：
            select * from pms_attr_group
            where catelog_id = catelogId and (attr_group_id = key or attr_group_name like '%key%')
             */
            QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
            Object key = params.get("key");
            if (!StringUtils.isEmpty(key)) {
                wrapper.and((obj) -> {
                    obj.eq("attr_group_id", key).or().like("attr_group_name", key);
                });
            }
            page = this.page(queryPage, wrapper);
        }
        return new PageUtils(page);
    }

}