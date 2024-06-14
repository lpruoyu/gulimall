package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrAttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2024-05-20 09:42:08
 */
@Slf4j
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * /product/attrgroup/{catelogId}/withattr
     * 获取该分类下所有分组&以及每一个分组关联的所有属性
     */
    @GetMapping("/{catelogId}/withattr")
    public R attrGroupsWithAttrs(@PathVariable("catelogId") Long catelogId) {
        List<AttrGroupWithAttrsVo> data = attrGroupService.queryAttrGroupsWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", data);
    }

    /**
     * /product/attrgroup/attr/relation/delete
     * 删除属性与分组的关联关系
     * 请求：[{"attrId":1,"attrGroupId":2}]
     */
//    @PostMapping("/attr/relation/delete")
//    public R batchDeleteRelation(@RequestBody AttrAttrGroupRelationVo[] relationVos) {
//        relationService.batchDeleteRelation(relationVos);
//        return R.ok();
//    }
    @PostMapping("/attr/relation/delete")
    public R batchDeleteRelation(@RequestBody List<AttrAttrGroupRelationVo> relationVos) {
        relationService.batchDeleteRelation(relationVos);
        return R.ok();
    }

    /**
     * 获取本分类下，该属性分组还没有关联的其他基本属性
     *
     * /product/attrgroup/{attrgroupId}/noattr/relation
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrGroupNoRelationAttrs(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId) {
        PageUtils page = attrService.queryNoRelationAttrsByAttrgroupId(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 获取属性分组的关联的所有属性
     * /product/attrgroup/{attrgroupId}/attr/relation
     */
    @RequestMapping("/{attrgroupId}/attr/relation")
    public R attrGroupRelationAttrs(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> data = attrService.queryAttrsByAttrgroupId(attrgroupId);
        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
//    @RequestMapping("/list")
//    // @RequiresPermissions("product:attrgroup:list")
//    public R list(@RequestParam Map<String, Object> params){
//        PageUtils page = attrGroupService.queryPage(params);
//
//        return R.ok().put("page", page);
//    }
    @RequestMapping("/list/{catelogId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 添加属性与分组关联关系
     * /product/attrgroup/attr/relation
     */
    @PostMapping("/attr/relation")
    public R batchAddRelation(@RequestBody List<AttrAttrGroupRelationVo> relationVos) {
        relationService.batchAddRelation(relationVos);
        return R.ok();
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //  @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
