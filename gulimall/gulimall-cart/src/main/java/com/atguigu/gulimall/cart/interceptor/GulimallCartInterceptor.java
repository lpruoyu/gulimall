package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 判断用户的登录状态。并封装传递(用户信息)给 controller。命令浏览器保存user-key这个Cookie
 */
public class GulimallCartInterceptor implements HandlerInterceptor {

    //   ThreadLocal: 同一个线程共享数据，可以让Controller等，快速得到用户信息UserInfoTo
    public static final ThreadLocal<UserInfoTo> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 目标方法执行之前
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();

//        request是SpringSession已经包装过的
        MemberRespVo member = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (null != member) { // 登录了
            userInfoTo.setUserId(member.getId());
//            利用GULISESSION这个Cookie(SpringSession配置的)，也就是sessionId作为user-key合适吗？
//            不合适
//            虽然能判断并获取这个信息，但是你使用了认证服务的信息，符合微服务分模块开发吗？
//            而且这样用的话，会增加复杂性
//            比如在用户没有登陆的情况下，userInfoTo的user-key一定会被设置过了，并且浏览器也保存了这个user-key
//            然后用户再次登录，不能将这个信息作为user-key直接使用了，又得给UserInfoTo再增添一个字段,比如叫userSessionId,反正很麻烦
//            Cookie[] cookies = request.getCookies();
//            if (cookies != null && cookies.length > 0) {
//                for (Cookie cookie : cookies) {
//                    if (cookie.getName().equals("GULISESSION")) {
//                        System.out.println(cookie.getName() + "====>" + cookie.getValue());
//                        break;
//                    }
//                }
//            }
        }

//        判断浏览器有没有带来user-key这个Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
//                  浏览器有带来user-key这个cookie（不是第一次使用购物车页面）
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    // 浏览器已经保存了user-key这个Cookie，那么就不需要浏览器再次保存了，如果不设置，那么这个Cookie会无限续期
                    userInfoTo.setFlag(true);
                    break;
                }
            }
        }

//        只要没有user-key，不管你有没有登录，就代表是第一次使用购物车页面，都给你生成一个user-key
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) { // 是第一次使用购物车页面
            String userKey = UUID.randomUUID().toString();
            userInfoTo.setUserKey(userKey);
        }

        THREAD_LOCAL.set(userInfoTo);

        return true;
    }

    /**
     * 业务执行之后；分配临时用户，让浏览器保存user-key
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = THREAD_LOCAL.get();

        //如果没有临时用户一定要让浏览器保存一个临时用户
        if (!userInfoTo.isFlag()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            cookie.setDomain("gulimall.com");
            response.addCookie(cookie); // 让浏览器保存user-key这个Cookie
        }
    }

}
