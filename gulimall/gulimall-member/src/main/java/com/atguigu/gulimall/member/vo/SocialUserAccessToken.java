package com.atguigu.gulimall.member.vo;

import lombok.Data;

@Data
public class SocialUserAccessToken {
    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
}
