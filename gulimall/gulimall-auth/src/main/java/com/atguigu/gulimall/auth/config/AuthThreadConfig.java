package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class AuthThreadConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        int corePoolSize = availableProcessors + 1;
        return new ThreadPoolExecutor(
                corePoolSize,
                100,
                3600,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

}