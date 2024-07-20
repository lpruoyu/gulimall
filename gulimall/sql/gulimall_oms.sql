create table oms_order
(
    id                      bigint auto_increment comment 'id'
        primary key,
    member_id               bigint         null comment 'member_id',
    order_sn                char(100)      null comment '订单号',
    coupon_id               bigint         null comment '使用的优惠券',
    create_time             datetime       null comment 'create_time',
    member_username         varchar(200)   null comment '用户名',
    total_amount            decimal(18, 4) null comment '订单总额',
    pay_amount              decimal(18, 4) null comment '应付总额',
    freight_amount          decimal(18, 4) null comment '运费金额',
    promotion_amount        decimal(18, 4) null comment '促销优化金额（促销价、满减、阶梯价）',
    integration_amount      decimal(18, 4) null comment '积分抵扣金额',
    coupon_amount           decimal(18, 4) null comment '优惠券抵扣金额',
    discount_amount         decimal(18, 4) null comment '后台调整订单使用的折扣金额',
    pay_type                tinyint        null comment '支付方式【1->支付宝；2->微信；3->银联； 4->货到付款；】',
    source_type             tinyint        null comment '订单来源[0->PC订单；1->app订单]',
    status                  tinyint        null comment '订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】',
    delivery_company        varchar(64)    null comment '物流公司(配送方式)',
    delivery_sn             varchar(64)    null comment '物流单号',
    auto_confirm_day        int            null comment '自动确认时间（天）',
    integration             int            null comment '可以获得的积分',
    growth                  int            null comment '可以获得的成长值',
    bill_type               tinyint        null comment '发票类型[0->不开发票；1->电子发票；2->纸质发票]',
    bill_header             varchar(255)   null comment '发票抬头',
    bill_content            varchar(255)   null comment '发票内容',
    bill_receiver_phone     varchar(32)    null comment '收票人电话',
    bill_receiver_email     varchar(64)    null comment '收票人邮箱',
    receiver_name           varchar(100)   null comment '收货人姓名',
    receiver_phone          varchar(32)    null comment '收货人电话',
    receiver_post_code      varchar(32)    null comment '收货人邮编',
    receiver_province       varchar(32)    null comment '省份/直辖市',
    receiver_city           varchar(32)    null comment '城市',
    receiver_region         varchar(32)    null comment '区',
    receiver_detail_address varchar(200)   null comment '详细地址',
    note                    varchar(500)   null comment '订单备注',
    confirm_status          tinyint        null comment '确认收货状态[0->未确认；1->已确认]',
    delete_status           tinyint        null comment '删除状态【0->未删除；1->已删除】',
    use_integration         int            null comment '下单时使用的积分',
    payment_time            datetime       null comment '支付时间',
    delivery_time           datetime       null comment '发货时间',
    receive_time            datetime       null comment '确认收货时间',
    comment_time            datetime       null comment '评价时间',
    modify_time             datetime       null comment '修改时间'
)
    comment '订单';

create table oms_order_item
(
    id                 bigint auto_increment comment 'id'
        primary key,
    order_id           bigint         null comment 'order_id',
    order_sn           char(100)      null comment 'order_sn',
    spu_id             bigint         null comment 'spu_id',
    spu_name           varchar(255)   null comment 'spu_name',
    spu_pic            varchar(500)   null comment 'spu_pic',
    spu_brand          varchar(200)   null comment '品牌',
    category_id        bigint         null comment '商品分类id',
    sku_id             bigint         null comment '商品sku编号',
    sku_name           varchar(255)   null comment '商品sku名字',
    sku_pic            varchar(500)   null comment '商品sku图片',
    sku_price          decimal(18, 4) null comment '商品sku价格',
    sku_quantity       int            null comment '商品购买的数量',
    sku_attrs_vals     varchar(500)   null comment '商品销售属性组合（JSON）',
    promotion_amount   decimal(18, 4) null comment '商品促销分解金额',
    coupon_amount      decimal(18, 4) null comment '优惠券优惠分解金额',
    integration_amount decimal(18, 4) null comment '积分优惠分解金额',
    real_amount        decimal(18, 4) null comment '该商品经过优惠后的分解金额',
    gift_integration   int            null comment '赠送积分',
    gift_growth        int            null comment '赠送成长值'
)
    comment '订单项信息';

create table oms_order_operate_history
(
    id           bigint auto_increment comment 'id'
        primary key,
    order_id     bigint       null comment '订单id',
    operate_man  varchar(100) null comment '操作人[用户；系统；后台管理员]',
    create_time  datetime     null comment '操作时间',
    order_status tinyint      null comment '订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】',
    note         varchar(500) null comment '备注'
)
    comment '订单操作历史记录';

create table oms_order_return_apply
(
    id              bigint auto_increment comment 'id'
        primary key,
    order_id        bigint         null comment 'order_id',
    sku_id          bigint         null comment '退货商品id',
    order_sn        char(32)       null comment '订单编号',
    create_time     datetime       null comment '申请时间',
    member_username varchar(64)    null comment '会员用户名',
    return_amount   decimal(18, 4) null comment '退款金额',
    return_name     varchar(100)   null comment '退货人姓名',
    return_phone    varchar(20)    null comment '退货人电话',
    status          tinyint(1)     null comment '申请状态[0->待处理；1->退货中；2->已完成；3->已拒绝]',
    handle_time     datetime       null comment '处理时间',
    sku_img         varchar(500)   null comment '商品图片',
    sku_name        varchar(200)   null comment '商品名称',
    sku_brand       varchar(200)   null comment '商品品牌',
    sku_attrs_vals  varchar(500)   null comment '商品销售属性(JSON)',
    sku_count       int            null comment '退货数量',
    sku_price       decimal(18, 4) null comment '商品单价',
    sku_real_price  decimal(18, 4) null comment '商品实际支付单价',
    reason          varchar(200)   null comment '原因',
    description述    varchar(500)   null comment '描述',
    desc_pics       varchar(2000)  null comment '凭证图片，以逗号隔开',
    handle_note     varchar(500)   null comment '处理备注',
    handle_man      varchar(200)   null comment '处理人员',
    receive_man     varchar(100)   null comment '收货人',
    receive_time    datetime       null comment '收货时间',
    receive_note    varchar(500)   null comment '收货备注',
    receive_phone   varchar(20)    null comment '收货电话',
    company_address varchar(500)   null comment '公司收货地址'
)
    comment '订单退货申请';

create table oms_order_return_reason
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(200) null comment '退货原因名',
    sort        int          null comment '排序',
    status      tinyint(1)   null comment '启用状态',
    create_time datetime     null comment 'create_time'
)
    comment '退货原因';

create table oms_order_setting
(
    id                    bigint auto_increment comment 'id'
        primary key,
    flash_order_overtime  int        null comment '秒杀订单超时关闭时间(分)',
    normal_order_overtime int        null comment '正常订单超时时间(分)',
    confirm_overtime      int        null comment '发货后自动确认收货时间（天）',
    finish_overtime       int        null comment '自动完成交易时间，不能申请退货（天）',
    comment_overtime      int        null comment '订单完成后自动好评时间（天）',
    member_level          tinyint(2) null comment '会员等级【0-不限会员等级，全部通用；其他-对应的其他会员等级】'
)
    comment '订单配置信息';

create table oms_payment_info
(
    id               bigint auto_increment comment 'id'
        primary key,
    order_sn         char(32)       null comment '订单号（对外业务号）',
    order_id         bigint         null comment '订单id',
    alipay_trade_no  varchar(50)    null comment '支付宝交易流水号',
    total_amount     decimal(18, 4) null comment '支付总金额',
    subject          varchar(200)   null comment '交易内容',
    payment_status   varchar(20)    null comment '支付状态',
    create_time      datetime       null comment '创建时间',
    confirm_time     datetime       null comment '确认时间',
    callback_content varchar(4000)  null comment '回调内容',
    callback_time    datetime       null comment '回调时间'
)
    comment '支付信息表';

create table oms_refund_info
(
    id              bigint auto_increment comment 'id'
        primary key,
    order_return_id bigint         null comment '退款的订单',
    refund          decimal(18, 4) null comment '退款金额',
    refund_sn       varchar(64)    null comment '退款交易流水号',
    refund_status   tinyint(1)     null comment '退款状态',
    refund_channel  tinyint        null comment '退款渠道[1-支付宝，2-微信，3-银联，4-汇款]',
    refund_content  varchar(5000)  null
)
    comment '退款信息';

create table undo_log
(
    branch_id     bigint       not null comment 'branch transaction id',
    xid           varchar(128) not null comment 'global transaction id',
    context       varchar(128) not null comment 'undo_log context,such as serialization',
    rollback_info longblob     not null comment 'rollback info',
    log_status    int          not null comment '0:normal status,1:defense status',
    log_created   datetime(6)  not null comment 'create datetime',
    log_modified  datetime(6)  not null comment 'modify datetime',
    constraint ux_undo_log
        unique (xid, branch_id)
)
    comment 'AT transaction mode undo table';

create index ix_log_created
    on undo_log (log_created);

