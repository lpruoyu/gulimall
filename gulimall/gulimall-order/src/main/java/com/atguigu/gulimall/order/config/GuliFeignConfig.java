package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return template -> {
            System.out.println("RequestInterceptor线程...." + Thread.currentThread().getId());
            // 通过RequestContextHolder拿到刚进来的这个请求
            // 通过RequestContextHolder获取到的RequestAttributes是Spring自动设置的
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest(); //老请求
                //同步请求头数据，Cookie
                String cookie = request.getHeader("Cookie");
                template.header("Cookie", cookie); //给新请求同步了老请求的cookie
            }
        };
    }

}