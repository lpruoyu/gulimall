package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.service.SpuInfoService;
import com.atguigu.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * spu信息
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2024-05-20 09:42:08
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**商品上架 POST
     * /product/spuinfo/{spuId}/up
     */
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId) {
        spuInfoService.up(spuId);
        return R.ok();
    }

    /**
     * 列表
     * spu检索
     * /product/spuinfo/list
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 新增商品
     * /product/spuinfo/save
     */
    @PostMapping("/save")
    //  @RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuSaveVo vo){
		spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
