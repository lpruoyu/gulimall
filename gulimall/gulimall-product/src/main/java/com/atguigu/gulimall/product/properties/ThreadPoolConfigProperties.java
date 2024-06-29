package com.atguigu.gulimall.product.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("thread")
@Component
@Data
public class ThreadPoolConfigProperties {
    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveTime;
    private int queueCapacity;
}
