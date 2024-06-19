package com.atguigu.gulimall.ware.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.vo.MergePurchaseVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2024-05-22 09:38:36
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * /ware/purchase/done
     * 完成采购
     * 请求参数：
     * {
     *    id: 123,//采购单id
     *    items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     * }
     * 请求体：
        {
        "id": 1,
        "items": [{"itemId":1,"status":3},{"itemId":2,"status":4,"reason":"没钱"},{"itemId":3,"status":3}]
        }
     */
    @PostMapping("/done")
    public R donePurchase(@RequestBody PurchaseDoneVo purchaseDoneVo){
        purchaseService.donePurchase(purchaseDoneVo);
        return R.ok();
    }
    /**
     * /ware/purchase/received
     * 领取采购单
     * 请求参数 [1,2,3,4]//采购单id
     */
    @PostMapping("/received")
    public R receivePurchase(@RequestBody List<Long> ids){
        purchaseService.receivePurchase(ids);
        return R.ok();
    }

    /**
     * /ware/purchase/unreceive/list
     * 查询未领取的采购单
     */
    @GetMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryUnreceivePage(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //  @RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
        Date date = new Date();
        purchase.setCreateTime(date);
        purchase.setUpdateTime(date);
        purchaseService.save(purchase);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * /ware/purchase/merge
     * 请求方法:
     * POST
     *
     * {
     *   purchaseId: 1, //整单id【没有提交采购单id的话，需要创建一个采购新单】
     *   items:[1,2,3,4] //采购需求id合并项集合
     * }
     */
    @PostMapping ("/merge")
    // @RequiresPermissions("ware:purchase:update")
    public R mergePurchase(@RequestBody MergePurchaseVo vo){
        purchaseService.mergePurchase(vo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
