package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.vo.AttrAttrGroupRelationVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2024-05-20 09:42:08
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    /*
    一句一句删除的话，会发送很多个请求给MySQL服务器，会对MySQL造成较大的性能影响，也会占用资源和带宽，所以使用批量删除

    批量删除语句：
    DELETE FROM pms_attr_attrgroup_relation
    WHERE
    ('attr_id' = 1 AND 'attr_group_id' = 1)
    OR
    ('attr_id' = 3 AND 'attr_group_id' = 2)
    OR ...
     */

    void batchDeleteRelation(@Param("relations") List<AttrAttrGroupRelationVo> relationVos);

//    void batchAddRelation(@Param("relations") List<AttrAttrGroupRelationVo> relationVos);
}
