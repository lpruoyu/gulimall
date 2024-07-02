package com.atguigu.gulimall.member.exception;

public class UserErrorException extends RuntimeException {
    public UserErrorException() {
        super("用户不存在");
    }
}
