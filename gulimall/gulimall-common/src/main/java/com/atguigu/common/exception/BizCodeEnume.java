package com.atguigu.common.exception;

/***
 * TODO 写博客
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5位数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。
 *      10:通用             000:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 *  10: 通用
 *      001：参数格式校验
 *  11: 商品
 *  12: 订单
 *  13: 购物车
 *  14: 物流
 */
public enum BizCodeEnume {
    SUCCESS(0, "OK"),
    HTTP_SUCCESS(200, "OK"),

    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    TOO_MANY_REQUEST(10002,"请求流量过大"),
    SMS_MULTI_EXCEPTION(10003,"验证码获取频率太高，请1分钟后再试"),
    SMS_SEND_EXCEPTION(10004,"验证码发送失败"),
    SMS_CODE_EXCEPTION(10005,"验证码错误"),
    REG_ERROR_EXCEPTION(10006,"用户名或手机已存在，注册失败"),
    USER_ERROR_EXCEPTION(10007,"用户不存在"),
    PASSWORD_ERROR_EXCEPTION(10008,"密码错误"),

    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号存在"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    LOGINACCT_PASSWORD_INVAILD_EXCEPTION(15003,"账号密码错误");

    private final int code;
    private final String msg;

    BizCodeEnume(int code,String msg){
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