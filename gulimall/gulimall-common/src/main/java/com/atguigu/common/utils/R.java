/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.atguigu.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.BizCodeEnume;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class R extends HashMap<String, Object> {
    public static final String CODE = "code";
    public static final String MSG = "msg";
    public static final String DATA = "data";

    //利用fastjson进行逆转
    public <T> T getData(String key, TypeReference<T> typeReference) {
        Object data = get(key);// 默认是map
        String s = JSON.toJSONString(data); // 得转为JSON字符串
        T t = JSON.parseObject(s, typeReference);
        return t;
    }

    //利用fastjson进行逆转
    public <T> T getData(TypeReference<T> typeReference) {
        return getData(DATA, typeReference);
    }

    public R setData(Object data) {
        put(DATA, data);
        return this;
    }

    public R() {
        put(CODE, BizCodeEnume.SUCCESS.getCode());
        put(MSG, BizCodeEnume.SUCCESS.getMsg());
    }

    public static R error() {
        return error("服务器未知异常，请联系管理员");
    }

    public static R error(String msg) {
//        500
        return error(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put(CODE, code);
        r.put(MSG, msg);
        return r;
    }

    public static R error(BizCodeEnume bizCodeEnume) {
        R r = new R();
        r.put(CODE, bizCodeEnume.getCode());
        r.put(MSG, bizCodeEnume.getMsg());
        return r;
    }

    public static R ok(String msg) {
        R r = new R();
        r.put(MSG, msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public Integer getCode() {
        return (Integer) this.get(CODE);
    }

    public String getMsg() {
        return (String) this.get(MSG);
    }
}
