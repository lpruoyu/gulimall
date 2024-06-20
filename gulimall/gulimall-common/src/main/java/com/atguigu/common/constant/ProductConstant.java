package com.atguigu.common.constant;

public final class ProductConstant {
    private ProductConstant() {
    }

    public enum ATTR {
        /*
        create table gulimall_pms.pms_attr
        (
            attr_id      bigint auto_increment comment '属性id'
                primary key,
            attr_name    char(30)      null comment '属性名',
            search_type  tinyint       null comment '是否需要检索[0-不需要，1-需要]',
            icon         varchar(255)  null comment '属性图标',
            value_select char(255)     null comment '可选值列表[用逗号分隔]',
            attr_type    tinyint       null comment '属性类型[0-销售属性，1-基本属性]',
            enable       bigint        null comment '启用状态[0 - 禁用，1 - 启用]',
            catelog_id   bigint        null comment '所属分类',
            show_desc    tinyint       null comment '快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整',
            value_type   int default 0 null comment '值类型[0-为单个值，1-可以选择多个值]'
        )   comment '商品属性';
         */
        ATTR_BASE(1, "基本属性", "base"), ATTR_SALE(0, "销售属性", "sale");

        private final String msg;
        private final String type;
        private final int code;

        ATTR(int code, String msg, String type) {
            this.code = code;
            this.msg = msg;
            this.type = type;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        public String getType() {
            return type;
        }
    }

    public enum  StatusEnum{
        NEW_SPU(0,"新建"),SPU_UP(1,"商品上架"),SPU_DOWN(2,"商品下架");
        private int code;
        private String msg;

        StatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
