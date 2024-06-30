package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.to.EmailTo;
import com.atguigu.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@RestController
public class SendEmailController {

    @Autowired
    private Properties props;

    @Autowired
    private Authenticator authenticator;

    @PostMapping("/email/send")
    public R sendEmail(@RequestBody EmailTo emailTo) throws MessagingException {
        // 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress form = new InternetAddress(props.getProperty("mail.user"));
        message.setFrom(form);
        // 设置收件人的邮箱
        InternetAddress to = new InternetAddress(emailTo.getReceiveMail());
        message.setRecipient(Message.RecipientType.TO, to);
        // 设置邮件标题
        message.setSubject(emailTo.getSubject());
        // 设置邮件的内容体
        message.setContent(emailTo.getContent(), "text/html;charset=UTF-8");
        // 最后当然就是发送邮件啦
        Transport.send(message);

        return R.ok();
    }

}
