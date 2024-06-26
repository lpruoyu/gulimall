package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.MergePurchaseVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2024-05-22 09:38:36
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryUnreceivePage(Map<String, Object> params);

    void mergePurchase(MergePurchaseVo vo);

    void receivePurchase(List<Long> ids);

    void donePurchase(PurchaseDoneVo purchaseDoneVo);
}

