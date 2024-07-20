create table undo_log
(
    id            bigint auto_increment
        primary key,
    branch_id     bigint       not null,
    xid           varchar(100) not null,
    context       varchar(128) not null,
    rollback_info longblob     not null,
    log_status    int          not null,
    log_created   datetime     not null,
    log_modified  datetime     not null,
    ext           varchar(100) null,
    constraint ux_undo_log
        unique (xid, branch_id)
)
    charset = utf8;

create index ix_log_created
    on undo_log (log_created);

create table wms_purchase
(
    id            bigint auto_increment
        primary key,
    assignee_id   bigint         null,
    assignee_name varchar(255)   null,
    phone         char(13)       null,
    priority      int(4)         null,
    status        int(4)         null,
    ware_id       bigint         null,
    amount        decimal(18, 4) null,
    create_time   datetime       null,
    update_time   datetime       null
)
    comment '采购信息';

create table wms_purchase_detail
(
    id          bigint auto_increment
        primary key,
    purchase_id bigint         null comment '采购单id',
    sku_id      bigint         null comment '采购商品id',
    sku_num     int            null comment '采购数量',
    sku_price   decimal(18, 4) null comment '采购金额',
    ware_id     bigint         null comment '仓库id',
    status      int            null comment '状态[0新建，1已分配，2正在采购，3已完成，4采购失败]'
);

create table wms_ware_info
(
    id       bigint auto_increment comment 'id'
        primary key,
    name     varchar(255) null comment '仓库名',
    address  varchar(255) null comment '仓库地址',
    areacode varchar(20)  null comment '区域编码'
)
    comment '仓库信息';

create table wms_ware_order_task
(
    id               bigint auto_increment comment 'id'
        primary key,
    order_id         bigint       null comment 'order_id',
    order_sn         varchar(255) null comment 'order_sn',
    consignee        varchar(100) null comment '收货人',
    consignee_tel    char(15)     null comment '收货人电话',
    delivery_address varchar(500) null comment '配送地址',
    order_comment    varchar(200) null comment '订单备注',
    payment_way      tinyint(1)   null comment '付款方式【 1:在线付款 2:货到付款】',
    task_status      tinyint(2)   null comment '任务状态',
    order_body       varchar(255) null comment '订单描述',
    tracking_no      char(30)     null comment '物流单号',
    create_time      datetime     null comment 'create_time',
    ware_id          bigint       null comment '仓库id',
    task_comment     varchar(500) null comment '工作单备注'
)
    comment '库存工作单';

create table wms_ware_order_task_detail
(
    id          bigint auto_increment comment 'id'
        primary key,
    sku_id      bigint       null comment 'sku_id',
    sku_name    varchar(255) null comment 'sku_name',
    sku_num     int          null comment '购买个数',
    task_id     bigint       null comment '工作单id',
    ware_id     bigint       null comment '仓库id',
    lock_status int(1)       null comment '1-已锁定  2-已解锁  3-扣减'
)
    comment '库存工作单';

create table wms_ware_sku
(
    id           bigint auto_increment comment 'id'
        primary key,
    sku_id       bigint        null comment 'sku_id',
    ware_id      bigint        null comment '仓库id',
    stock        int           null comment '库存数',
    sku_name     varchar(200)  null comment 'sku_name',
    stock_locked int default 0 null comment '锁定库存'
)
    comment '商品库存';

create index sku_id
    on wms_ware_sku (sku_id);

create index ware_id
    on wms_ware_sku (ware_id);

