package com.atguigu.gulimall.member.exception;

//TODO 写博客
public class UsernameExistException extends RuntimeException {
    public UsernameExistException() {
        super("用户名存在");
    }
}
