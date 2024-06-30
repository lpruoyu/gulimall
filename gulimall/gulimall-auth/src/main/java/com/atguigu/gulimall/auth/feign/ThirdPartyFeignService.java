package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.to.EmailTo;
import com.atguigu.common.utils.R;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {
    @GetMapping("/sms/send")
    R sendSms(@RequestParam("phone") String phone, @RequestParam("code") String code);

    @PostMapping("/email/send")
    R sendEmail(@RequestBody EmailTo emailVo) throws MessagingException;
}
