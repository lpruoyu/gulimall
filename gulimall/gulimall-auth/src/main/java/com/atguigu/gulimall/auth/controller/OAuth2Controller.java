package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.vo.SocialUserAccessToken;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 */
@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 微博用户授权成功回调
     * http://auth.gulimall.com/oauth2.0/weibo/success?code=7d42ee3197927a8807460280d86152cd
     * 拿到code换取Access Token：其中client_id=App Key&client_secret=App Secret
     * POST：https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
     * 拿到Access Token就可以获取用户信息：
     * https://api.weibo.com/2/users/show.json?uid=xxxx&access_token=xxxx
     */

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        bodys.put("client_id", "3276999101");
        bodys.put("client_secret", "452bbefff4680ac8554b97799a8c12cb");
        bodys.put("grant_type", "authorization_code");
        bodys.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        bodys.put("code", code);
        //1、根据code换取accessToken；
        HttpResponse response = HttpUtils.doPost(
                "https://api.weibo.com",
                "/oauth2/access_token",
                headers,
                null,
                bodys);
        if (response.getStatusLine().getStatusCode() == 200) {
            //2、获取到了 socialUserAccessToken 进行处理
            String json = EntityUtils.toString(response.getEntity());
            SocialUserAccessToken socialUserAccessToken = JSON.parseObject(json, SocialUserAccessToken.class);
//            String uid = socialUserAccessToken.getUid();
            // 通过uid就知道当前是哪个社交用户
            //1）、当前用户如果是第一次进网站，进行自动注册(为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员账号)
            R r = memberFeignService.socialLogin(socialUserAccessToken);
            if(r.getCode() == BizCodeEnume.SUCCESS.getCode()) {
                //登录或者注册这个社交用户
                //2）、登录成功就跳回首页
                return "redirect:http://gulimall.com";
            }
        }
        return "redirect:http://auth.gulimall.com/login.html";
    }

}
