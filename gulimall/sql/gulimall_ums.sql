create table ums_growth_change_history
(
    id           bigint auto_increment comment 'id'
        primary key,
    member_id    bigint     null comment 'member_id',
    create_time  datetime   null comment 'create_time',
    change_count int        null comment '改变的值（正负计数）',
    note         varchar(0) null comment '备注',
    source_type  tinyint    null comment '积分来源[0-购物，1-管理员修改]'
)
    comment '成长值变化历史记录';

create table ums_integration_change_history
(
    id           bigint auto_increment comment 'id'
        primary key,
    member_id    bigint       null comment 'member_id',
    create_time  datetime     null comment 'create_time',
    change_count int          null comment '变化的值',
    note         varchar(255) null comment '备注',
    source_tyoe  tinyint      null comment '来源[0->购物；1->管理员修改;2->活动]'
)
    comment '积分变化历史记录';

create table ums_member
(
    id           bigint auto_increment comment 'id'
        primary key,
    level_id     bigint       null comment '会员等级id',
    username     char(64)     null comment '用户名',
    password     varchar(64)  null comment '密码',
    nickname     varchar(64)  null comment '昵称',
    mobile       varchar(20)  null comment '手机号码',
    email        varchar(64)  null comment '邮箱',
    header       varchar(500) null comment '头像',
    gender       tinyint      null comment '性别',
    birth        date         null comment '生日',
    city         varchar(500) null comment '所在城市',
    job          varchar(255) null comment '职业',
    sign         varchar(255) null comment '个性签名',
    source_type  tinyint      null comment '用户来源',
    integration  int          null comment '积分',
    growth       int          null comment '成长值',
    status       tinyint      null comment '启用状态',
    create_time  datetime     null comment '注册时间',
    social_uid   varchar(255) null comment '社交网站的用户ID',
    access_token varchar(255) null comment '社交网站访问数据的令牌',
    expires_in   varchar(255) null comment '社网站令牌的过期时间'
)
    comment '会员';

create table ums_member_collect_spu
(
    id          bigint       not null comment 'id'
        primary key,
    member_id   bigint       null comment '会员id',
    spu_id      bigint       null comment 'spu_id',
    spu_name    varchar(500) null comment 'spu_name',
    spu_img     varchar(500) null comment 'spu_img',
    create_time datetime     null comment 'create_time'
)
    comment '会员收藏的商品';

create table ums_member_collect_subject
(
    id           bigint auto_increment comment 'id'
        primary key,
    subject_id   bigint       null comment 'subject_id',
    subject_name varchar(255) null comment 'subject_name',
    subject_img  varchar(500) null comment 'subject_img',
    subject_urll varchar(500) null comment '活动url'
)
    comment '会员收藏的专题活动';

create table ums_member_level
(
    id                      bigint auto_increment comment 'id'
        primary key,
    name                    varchar(100)   null comment '等级名称',
    growth_point            int            null comment '等级需要的成长值',
    default_status          tinyint        null comment '是否为默认等级[0->不是；1->是]',
    free_freight_point      decimal(18, 4) null comment '免运费标准',
    comment_growth_point    int            null comment '每次评价获取的成长值',
    priviledge_free_freight tinyint        null comment '是否有免邮特权',
    priviledge_member_price tinyint        null comment '是否有会员价格特权',
    priviledge_birthday     tinyint        null comment '是否有生日特权',
    note                    varchar(255)   null comment '备注'
)
    comment '会员等级';

create table ums_member_login_log
(
    id          bigint auto_increment comment 'id'
        primary key,
    member_id   bigint      null comment 'member_id',
    create_time datetime    null comment '创建时间',
    ip          varchar(64) null comment 'ip',
    city        varchar(64) null comment 'city',
    login_type  tinyint(1)  null comment '登录类型[1-web，2-app]'
)
    comment '会员登录记录';

create table ums_member_receive_address
(
    id             bigint auto_increment comment 'id'
        primary key,
    member_id      bigint       null comment 'member_id',
    name           varchar(255) null comment '收货人姓名',
    phone          varchar(64)  null comment '电话',
    post_code      varchar(64)  null comment '邮政编码',
    province       varchar(100) null comment '省份/直辖市',
    city           varchar(100) null comment '城市',
    region         varchar(100) null comment '区',
    detail_address varchar(255) null comment '详细地址(街道)',
    areacode       varchar(15)  null comment '省市区代码',
    default_status tinyint(1)   null comment '是否默认'
)
    comment '会员收货地址';

create table ums_member_statistics_info
(
    id                    bigint auto_increment comment 'id'
        primary key,
    member_id             bigint         null comment '会员id',
    consume_amount        decimal(18, 4) null comment '累计消费金额',
    coupon_amount         decimal(18, 4) null comment '累计优惠金额',
    order_count           int            null comment '订单数量',
    coupon_count          int            null comment '优惠券数量',
    comment_count         int            null comment '评价数',
    return_order_count    int            null comment '退货数量',
    login_count           int            null comment '登录次数',
    attend_count          int            null comment '关注数量',
    fans_count            int            null comment '粉丝数量',
    collect_product_count int            null comment '收藏的商品数量',
    collect_subject_count int            null comment '收藏的专题活动数量',
    collect_comment_count int            null comment '收藏的评论数量',
    invite_friend_count   int            null comment '邀请的朋友数量'
)
    comment '会员统计信息';

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

