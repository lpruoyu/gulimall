package com.atguigu.gulimall.thirdparty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${mail.user}")
    private String mailUser;

    @Value("${mail.password}")
    private String mailPassword;

    @Bean
    public Properties props() {
        // 创建Properties 类用于记录邮箱的一些属性
        Properties props = new Properties();
        // 表示SMTP发送邮件，必须进行身份验证
        props.put("mail.smtp.auth", "true");
        //此处填写SMTP服务器
        props.put("mail.smtp.host", "smtp.qq.com");
        //端口号，QQ邮箱端口587
        props.put("mail.smtp.port", "587");
        // 此处填写，写信人的账号
        props.put("mail.user", mailUser);
        // 此处填写16位STMP口令
        props.put("mail.password", mailPassword);
        return props;
    }

    @Bean
    public Authenticator authenticator(Properties props) {
        // 构建授权信息，用于进行SMTP进行身份验证
        return new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                // 用户名、密码
                String userName = props.getProperty("mail.user");
                String password = props.getProperty("mail.password");
                return new PasswordAuthentication(userName, password);
            }
        };
    }
}
