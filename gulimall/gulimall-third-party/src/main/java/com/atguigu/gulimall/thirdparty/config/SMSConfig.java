package com.atguigu.gulimall.thirdparty.config;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SMSConfig {

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;
    @Value("${spring.cloud.alicloud.secret-key}")
    private String secretKey;

    @Bean
    public StaticCredentialProvider provider() {
       return StaticCredentialProvider.create(Credential.builder().accessKeyId(accessId).accessKeySecret(secretKey).build());
    }

}
