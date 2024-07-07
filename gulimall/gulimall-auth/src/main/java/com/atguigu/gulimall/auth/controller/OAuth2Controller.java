package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.vo.SocialUserAccessToken;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 */
@Controller
public class OAuth2Controller {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

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
    public String weibo(@RequestParam("code") String code, HttpSession session, HttpServletResponse httpServletResponse) throws Exception {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        bodys.put("client_id", "3276999101");
        bodys.put("client_secret", "452bbefff4680ac8554b97799a8c12cb");
        bodys.put("grant_type", "authorization_code");
        bodys.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        bodys.put("code", code);
        //1、根据code换取accessToken；
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", headers, null, bodys);
        if (response.getStatusLine().getStatusCode() == 200) {
            //2、获取到了 socialUserAccessToken 进行处理
            String json = EntityUtils.toString(response.getEntity());
            SocialUserAccessToken socialUserAccessToken = JSON.parseObject(json, SocialUserAccessToken.class);
//            String uid = socialUserAccessToken.getUid();
            // 通过uid就知道当前是哪个社交用户
            //1）、当前用户如果是第一次进网站，进行自动注册(为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员账号)
            R r = memberFeignService.socialLogin(socialUserAccessToken);
            if (r.getCode() == BizCodeEnume.SUCCESS.getCode()) {
                //登录或者注册这个社交用户
                //2）、登录成功就跳回首页

                MemberRespVo loginUser = r.getData(new TypeReference<MemberRespVo>() {
                });

//                /**
//                 * 手动设置Cookie
//                 */
//                Redis中不应该以loginUser作为用户的key，而是应该以UUID作为key来存储用户信息。
//                而且，我们在这儿实现是将用户转为JSON字符串存起来，为了后续方便取出用户这个对象信息，应该直接以对象的方式将用户保存起来，这样从Redis中取出来的数据对象直接就能使用
//                stringRedisTemplate.opsForValue().set("loginUser", JSON.toJSONString(loginUser));
//                Cookie cookie = new Cookie("GULIMALL", "loginUser");
//                cookie.setDomain("gulimall.com");
//                cookie.setMaxAge(24 * 60 * 60);
//                cookie.setPath("/");
//                httpServletResponse.addCookie(cookie);

//              5 使用SpringSession【跟以前使用session的写法一样】
                //第一次使用session；命令浏览器保存卡号。JSESSIONID这个cookie；
                //以后浏览器访问哪个网站就会带上这个网站的cookie；
                //子域之间； gulimall.com  auth.gulimall.com  order.gulimall.com
                //应该做到：发卡的时候(指定域名为父域名)，那么，即使是子域系统发的卡，也能让父域直接使用。
                // 1、默认发的令牌。session=dsajkdjl。作用域：当前域；（SpringSession默认没有解决子域session共享问题）
                // 2、使用JSON的序列化方式来序列化对象数据到redis中
                session.setAttribute(AuthServerConstant.LOGIN_USER, loginUser);

                return "redirect:http://gulimall.com";
            }
        }
        return "redirect:http://auth.gulimall.com/login.html";
    }

}
