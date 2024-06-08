package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2024-05-20 09:42:08
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 获取品牌关联的所有分类
     */
    @GetMapping("/catelog/list")
//    @RequestMapping(value = "/catelog/list", method = RequestMethod.GET)
    // @RequiresPermissions("product:categorybrandrelation:list")
    public R catelogList(@RequestParam("brandId") Long brandId) {
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService
                .list(
                        new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     *
     * 对数据库大表，比如pms_category_brand_relation   pms_brand    pms_category
     * pms_brand和pms_category这两张表的关联表pms_category_brand_relation
     * 设计的时候可以不用设计上brand_name和catelog_name
     * 但是每次需要使用的时候就去关联查询会对数据库的性能有非常大的影响
     * 所以，我们对于大表数据从不做关联，因此就可以设计这两个冗余字段
     * create table gulimall_pms.pms_category_brand_relation
     * (
     *     id           bigint auto_increment
     *         primary key,
     *     brand_id     bigint       null comment '品牌id',
     *     catelog_id   bigint       null comment '分类id',
     *     brand_name   varchar(255) null,
     *     catelog_name varchar(255) null
     * )   comment '品牌分类关联';
     *
     *
     *
     * 新增品牌与分类关联关系
     *
     * //IMPORTANT 如果某张表中有其他表的冗余字段，那么，当其他表中的数据发生了变化，
     *             那么，我们也要更新这张表中受影响的字段
     * //          品牌名发生变化，需要更新；分类名发生变化，需要更新；
     */
    @RequestMapping("/save")
    //  @RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
//        categoryBrandRelationService.save(categoryBrandRelation);

        categoryBrandRelationService.saveDetail(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
