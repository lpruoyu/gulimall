package com.atguigu.gulimall.cart.config;

import com.atguigu.gulimall.cart.interceptor.GulimallCartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GulimallCartWebConfig implements WebMvcConfigurer {
//    /**
//     * 视图映射
//     */
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/success.html").setViewName("success");
//        registry.addViewController("/cart.html").setViewName("cartList");
//    }

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new GulimallCartInterceptor()).addPathPatterns("/**");
    }
}