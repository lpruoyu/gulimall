package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey; //一定封装了，user-key 是随机生成的 id，不管有没有登录都会有这个 cookie 信息。

    private boolean flag = false; // 只需要让浏览器保存一次user-key这个cookie即可
}
