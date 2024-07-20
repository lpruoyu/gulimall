create table sms_coupon
(
    id                bigint auto_increment comment 'id'
        primary key,
    coupon_type       tinyint(1)     null comment '优惠卷类型[0->全场赠券；1->会员赠券；2->购物赠券；3->注册赠券]',
    coupon_img        varchar(2000)  null comment '优惠券图片',
    coupon_name       varchar(100)   null comment '优惠卷名字',
    num               int            null comment '数量',
    amount            decimal(18, 4) null comment '金额',
    per_limit         int            null comment '每人限领张数',
    min_point         decimal(18, 4) null comment '使用门槛',
    start_time        datetime       null comment '开始时间',
    end_time          datetime       null comment '结束时间',
    use_type          tinyint(1)     null comment '使用类型[0->全场通用；1->指定分类；2->指定商品]',
    note              varchar(200)   null comment '备注',
    publish_count     int            null comment '发行数量',
    use_count         int            null comment '已使用数量',
    receive_count     int            null comment '领取数量',
    enable_start_time datetime       null comment '可以领取的开始日期',
    enable_end_time   datetime       null comment '可以领取的结束日期',
    code              varchar(64)    null comment '优惠码',
    member_level      tinyint(1)     null comment '可以领取的会员等级[0->不限等级，其他-对应等级]',
    publish           tinyint(1)     null comment '发布状态[0-未发布，1-已发布]'
)
    comment '优惠券信息';

create table sms_coupon_history
(
    id               bigint auto_increment comment 'id'
        primary key,
    coupon_id        bigint      null comment '优惠券id',
    member_id        bigint      null comment '会员id',
    member_nick_name varchar(64) null comment '会员名字',
    get_type         tinyint(1)  null comment '获取方式[0->后台赠送；1->主动领取]',
    create_time      datetime    null comment '创建时间',
    use_type         tinyint(1)  null comment '使用状态[0->未使用；1->已使用；2->已过期]',
    use_time         datetime    null comment '使用时间',
    order_id         bigint      null comment '订单id',
    order_sn         bigint      null comment '订单号'
)
    comment '优惠券领取历史记录';

create table sms_coupon_spu_category_relation
(
    id            bigint auto_increment comment 'id'
        primary key,
    coupon_id     bigint      null comment '优惠券id',
    category_id   bigint      null comment '产品分类id',
    category_name varchar(64) null comment '产品分类名称'
)
    comment '优惠券分类关联';

create table sms_coupon_spu_relation
(
    id        bigint auto_increment comment 'id'
        primary key,
    coupon_id bigint       null comment '优惠券id',
    spu_id    bigint       null comment 'spu_id',
    spu_name  varchar(255) null comment 'spu_name'
)
    comment '优惠券与产品关联';

create table sms_home_adv
(
    id           bigint auto_increment comment 'id'
        primary key,
    name         varchar(100) null comment '名字',
    pic          varchar(500) null comment '图片地址',
    start_time   datetime     null comment '开始时间',
    end_time     datetime     null comment '结束时间',
    status       tinyint(1)   null comment '状态',
    click_count  int          null comment '点击数',
    url          varchar(500) null comment '广告详情连接地址',
    note         varchar(500) null comment '备注',
    sort         int          null comment '排序',
    publisher_id bigint       null comment '发布者',
    auth_id      bigint       null comment '审核者'
)
    comment '首页轮播广告';

create table sms_home_subject
(
    id        bigint auto_increment comment 'id'
        primary key,
    name      varchar(200) null comment '专题名字',
    title     varchar(255) null comment '专题标题',
    sub_title varchar(255) null comment '专题副标题',
    status    tinyint(1)   null comment '显示状态',
    url       varchar(500) null comment '详情连接',
    sort      int          null comment '排序',
    img       varchar(500) null comment '专题图片地址'
)
    comment '首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】';

create table sms_home_subject_spu
(
    id         bigint auto_increment comment 'id'
        primary key,
    name       varchar(200) null comment '专题名字',
    subject_id bigint       null comment '专题id',
    spu_id     bigint       null comment 'spu_id',
    sort       int          null comment '排序'
)
    comment '专题商品';

create table sms_member_price
(
    id                bigint auto_increment comment 'id'
        primary key,
    sku_id            bigint         null comment 'sku_id',
    member_level_id   bigint         null comment '会员等级id',
    member_level_name varchar(100)   null comment '会员等级名',
    member_price      decimal(18, 4) null comment '会员对应价格',
    add_other         tinyint(1)     null comment '可否叠加其他优惠[0-不可叠加优惠，1-可叠加]'
)
    comment '商品会员价格';

create table sms_seckill_promotion
(
    id          bigint auto_increment comment 'id'
        primary key,
    title       varchar(255) null comment '活动标题',
    start_time  datetime     null comment '开始日期',
    end_time    datetime     null comment '结束日期',
    status      tinyint      null comment '上下线状态',
    create_time datetime     null comment '创建时间',
    user_id     bigint       null comment '创建人'
)
    comment '秒杀活动';

create table sms_seckill_session
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(200) null comment '场次名称',
    start_time  datetime     null comment '每日开始时间',
    end_time    datetime     null comment '每日结束时间',
    status      tinyint(1)   null comment '启用状态',
    create_time datetime     null comment '创建时间'
)
    comment '秒杀活动场次';

create table sms_seckill_sku_notice
(
    id            bigint auto_increment comment 'id'
        primary key,
    member_id     bigint     null comment 'member_id',
    sku_id        bigint     null comment 'sku_id',
    session_id    bigint     null comment '活动场次id',
    subcribe_time datetime   null comment '订阅时间',
    send_time     datetime   null comment '发送时间',
    notice_type   tinyint(1) null comment '通知方式[0-短信，1-邮件]'
)
    comment '秒杀商品通知订阅';

create table sms_seckill_sku_relation
(
    id                   bigint auto_increment comment 'id'
        primary key,
    promotion_id         bigint  null comment '活动id',
    promotion_session_id bigint  null comment '活动场次id',
    sku_id               bigint  null comment '商品id',
    seckill_price        decimal null comment '秒杀价格',
    seckill_count        decimal null comment '秒杀总量',
    seckill_limit        decimal null comment '每人限购数量',
    seckill_sort         int     null comment '排序'
)
    comment '秒杀活动商品关联';

create table sms_sku_full_reduction
(
    id           bigint auto_increment comment 'id'
        primary key,
    sku_id       bigint         null comment 'spu_id',
    full_price   decimal(18, 4) null comment '满多少',
    reduce_price decimal(18, 4) null comment '减多少',
    add_other    tinyint(1)     null comment '是否参与其他优惠'
)
    comment '商品满减信息';

create table sms_sku_ladder
(
    id         bigint auto_increment comment 'id'
        primary key,
    sku_id     bigint         null comment 'spu_id',
    full_count int            null comment '满几件',
    discount   decimal(4, 2)  null comment '打几折',
    price      decimal(18, 4) null comment '折后价',
    add_other  tinyint(1)     null comment '是否叠加其他优惠[0-不可叠加，1-可叠加]'
)
    comment '商品阶梯价格';

create table sms_spu_bounds
(
    id          bigint auto_increment comment 'id'
        primary key,
    spu_id      bigint         null,
    grow_bounds decimal(18, 4) null comment '成长积分',
    buy_bounds  decimal(18, 4) null comment '购物积分',
    work        tinyint(1)     null comment '优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]'
)
    comment '商品spu积分设置';

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

