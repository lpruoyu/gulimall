package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergePurchaseVo {

    /**
     * {
     *   purchaseId: 1, //整单id【没有提交采购单id的话，需要创建一个采购新单】
     *   items:[1,2,3,4] //采购需求id合并项集合
     * }
     */

    private Long purchaseId;
    private List<Long> items;

}
