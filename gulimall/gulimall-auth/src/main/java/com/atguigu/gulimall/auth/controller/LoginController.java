package com.atguigu.gulimall.auth.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.to.EmailTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//@Controller
@RestController
public class LoginController {
//    @GetMapping("/login.html")
//    public String loginPage() {
//        return "login";
//    }
//
//    @GetMapping("/reg.html")
//    public String regPage() {
//        return "reg";
//    }


    @Autowired
    private ThreadPoolExecutor threadPool;

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/sms/sendEmail")
    public R sendEmailCode(@RequestParam("email") String email) throws MessagingException {
        String code = UUID.randomUUID().toString().substring(0, 5);
        String key = AuthServerConstant.EMAIL_CODE_CACHE_PREFIX + email;

        String oldCode = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(oldCode)) { // 说明5分钟内已经给该邮箱发送过验证码了
            long l = Long.parseLong(oldCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) { // 如果时间间隔小于60s
                return R.error(BizCodeEnume.SMS_MULTI_EXCEPTION);
            }
        }

        CompletableFuture.runAsync(() -> {
            // 给Redis放置验证码
            String realSaveCode = code + "_" + System.currentTimeMillis();
            stringRedisTemplate.opsForValue().set(key, realSaveCode, 5, TimeUnit.MINUTES); //过期时间5分钟
        }, threadPool);

        CompletableFuture.runAsync(() -> {
            // 发送邮件
            try {
                EmailTo emailTo = new EmailTo();
                emailTo.setReceiveMail(email);
                emailTo.setContent("验证码：" + code + "——有效期5分钟！");
                emailTo.setSubject("欢迎注册！");
                thirdPartyFeignService.sendEmail(emailTo);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }, threadPool);

        return R.ok();
    }


    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
//        Redis缓存验证码：存起来方便下次校验 以及 可以给验证码设置有效期

        String code = getRandomCode().toString();

//        防止同一个手机号在60s内再次发送验证码
        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone;

        String oldCode = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(oldCode)) {
            long l = Long.parseLong(oldCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) { // 如果时间间隔小于60s
                return R.error(BizCodeEnume.SMS_MULTI_EXCEPTION);
            }
        }

        R r = thirdPartyFeignService.sendSms(phone, code);
        if (r.getCode() == BizCodeEnume.SUCCESS.getCode()) {
            code = code + "_" + System.currentTimeMillis();
            stringRedisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES); //过期时间5分钟
        }
        return r;
    }

    private Integer getRandomCode() {
        //4位数字验证码：想要[1000,9999]，也就是[1000,10000)

        // Math.random() -> [0, 1)  // (int) Math.random()永远为0
        // Math.random() * (end - begin) -> [0, end - begin)
        // begin + Math.random() * (end - begin) -> [begin, end)
        int code = (int) (1000 + Math.random() * (10000 - 1000));
        return code;
    }

}
