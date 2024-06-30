package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {
    /**
     * 视图映射
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        /**
         *     @GetMapping("/login.html")
         *     public String loginPage(){
         *          //空方法
         *         return "login";
         *     }
         */
        //只是get请求能映射
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}