package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.to.EmailTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
//@RestController
public class LoginController {
    @GetMapping("/login.html")
    public String loginPage() {
//        if(stringRedisTemplate.opsForValue().get("loginUser") != null) return "redirect:http://gulimall.com";
        return "login";
    }
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
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/sms/sendEmail")
    @ResponseBody
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
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
//        Redis缓存验证码：存起来方便下次校验 以及 可以给验证码设置有效期

        final String code = getRandomCode().toString();

//        防止同一个手机号在60s内再次发送验证码
        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone;

        String redisSavedCode = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(redisSavedCode)) {
            long l = Long.parseLong(codeUnResolve(redisSavedCode)[1]);
            if (System.currentTimeMillis() - l < 60000) { // 如果时间间隔小于60s
                return R.error(BizCodeEnume.SMS_MULTI_EXCEPTION);
            }
        }

//        R r = thirdPartyFeignService.sendSms(phone, code);
//        if (r.getCode() == BizCodeEnume.SUCCESS.getCode()) {
//            code = code + "_" + System.currentTimeMillis();
//            stringRedisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES); //过期时间5分钟
//        }
//        return r;

        CompletableFuture.runAsync(() -> thirdPartyFeignService.sendSms(phone, code), threadPool);
        CompletableFuture.runAsync(() -> {
            stringRedisTemplate.opsForValue().set(key, codeResolve(code), 5, TimeUnit.MINUTES); //过期时间5分钟
        }, threadPool);
        return R.ok();
    }

    private String codeResolve(String code) {
        return code + "_" + System.currentTimeMillis();
    }

    private String[] codeUnResolve(String code) {
        return code.split("_");
    }

    private Integer getRandomCode() {
        //4位数字验证码：想要[1000,9999]，也就是[1000,10000)

        // Math.random() -> [0, 1)  // (int) Math.random()永远为0
        // Math.random() * (end - begin) -> [0, end - begin)
        // begin + Math.random() * (end - begin) -> [begin, end)
        int code = (int) (1000 + Math.random() * (10000 - 1000));
        return code;
    }

    /**
     * //重定向携带数据，是利用session原理。
     * 将数据放在session中，只要跳到下一个页面取出这个数据以后，session里面的数据就会删掉
     * <p>
     *  TODO 解决分布式下的session问题。
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) { //数据校验不通过
            /*
             * .map(fieldError -> {
             *                 String field = fieldError.getField();
             *                 String defaultMessage = fieldError.getDefaultMessage();
             *                 errors.put(field,defaultMessage);
             *                 return
             *             })
             */
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);

            //Request method 'POST' not supported
            //用户注册->/regist[post]----》转发/reg.html（路径映射默认都是get方式访问的。）
//            return "/reg.html";
            //直接
            //校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }

//        数据校验通过 校验验证码 (使用手机)
        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone();

//        String key = AuthServerConstant.EMAIL_CODE_CACHE_PREFIX + vo.getPhone(); //使用Email

        String redisSavedCode = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(redisSavedCode)) { // 不为空，说明给该用户发送过验证码，并且验证码没有过期
            String realCode = codeUnResolve(redisSavedCode)[0];
            if (vo.getCode().equals(realCode)) {
                // 验证码正确 删除验证码
                CompletableFuture.runAsync(() -> stringRedisTemplate.delete(key), threadPool);
                // 调用远程服务，真正注册
                R regist = memberFeignService.regist(vo);
                if (regist.getCode() == BizCodeEnume.SUCCESS.getCode())
//                     注册成功
                    return "redirect:http://auth.gulimall.com/login.html";
                else {
                    // 注册失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("code", regist.getCode().toString());
                    errors.put("msg", regist.getMsg());
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            }
        }
        Map<String, String> errors = new HashMap<>();
        errors.put("code", BizCodeEnume.SMS_CODE_EXCEPTION.getCode() + "");
        errors.put("msg", BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
        redirectAttributes.addFlashAttribute("errors", errors);
        return "redirect:http://auth.gulimall.com/reg.html";
    }


//    @GetMapping("/login.html")
//    public String loginPage(HttpSession session) {
//        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
//        if (attribute == null) {
//            //没登录
//            return "login";
//        } else {
//            return "redirect:http://gulimall.com";
//        }
//    }


    @PostMapping("/login")
    public String login(@Valid UserLoginVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) { //数据校验不通过
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        R login = memberFeignService.login(vo);
        if (login.getCode() == BizCodeEnume.SUCCESS.getCode()) {
            MemberRespVo data = login.getData(new TypeReference<MemberRespVo>() {
            });
            System.out.println("登录成功");
            return "redirect:http://gulimall.com";
        }
        return "redirect:http://auth.gulimall.com/login.html";
    }

}
