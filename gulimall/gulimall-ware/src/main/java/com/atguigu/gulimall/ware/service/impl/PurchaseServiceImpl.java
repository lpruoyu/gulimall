package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.vo.MergePurchaseVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), new QueryWrapper<PurchaseEntity>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryUnreceivePage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().in("status", 0, 1)
//                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergePurchaseVo vo) {
        //TODO 确认采购单状态是0,1(新建/已分配)才可以合并，

        /*
         * {
         *   purchaseId: 1, //整单id【没有提交采购单id的话，需要创建一个采购新单】
         *   items:[1,2,3,4] //采购需求id合并项集合
         * }
         */
        Long purchaseId = vo.getPurchaseId();
        if (null == purchaseId) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            Date date = new Date();
            purchaseEntity.setUpdateTime(date);
            purchaseEntity.setCreateTime(date);
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = vo.getItems().stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     *
     * @param ids 采购单id
     */
    @Transactional
    @Override
    public void receivePurchase(List<Long> ids) {
//        确认采购单状态是0,1(新建/已分配)
        Collection<PurchaseEntity> purchaseEntities = this.listByIds(ids);
        List<PurchaseEntity> collect = purchaseEntities.stream()
                .filter(purchaseEntity ->
                        purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                                || purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()
                )
                .map(purchaseEntity -> {
                    PurchaseEntity entity = new PurchaseEntity();
                    entity.setId(purchaseEntity.getId());
                    entity.setUpdateTime(new Date());
                    entity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    return entity;
                })
                .collect(Collectors.toList());
//        更新采购单状态
        this.updateBatchById(collect);
//        更新采购项状态
        ids.forEach(id -> {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByPurchaseId(id);
            List<PurchaseDetailEntity> detailEntities = purchaseDetailEntities.stream().map(purchaseDetailEntity -> {
                PurchaseDetailEntity entity = new PurchaseDetailEntity();
                entity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                entity.setId(purchaseDetailEntity.getId());
                return entity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

}
