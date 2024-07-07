package com.atguigu.gulimall.auth.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

//@Component
public class GulimallAuthSessionInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        判断浏览器有没有带来rsessionid这个Cookie
//        rsessionid起任何名都行，我在这儿模仿jsessionid
        boolean flag = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AuthServerConstant.REDIS_SESSION_ID_KEY)) {
//                    如果浏览器带来了rsessionid这个Cookie，代表Redis已经存着这个session对象了
                    THREAD_LOCAL.set(cookie.getValue());
                    flag = true;
                    break;
                }
            }
        }

        if (!flag) { // 如果没有带来rsessionid这个Cookie，给Redis中创建一个
            String s = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set(s, "");
            THREAD_LOCAL.set(s);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Cookie cookie = new Cookie(AuthServerConstant.REDIS_SESSION_ID_KEY, THREAD_LOCAL.get());
        cookie.setDomain("gulimall.com");
        response.addCookie(cookie); // 让浏览器保存这个rsessionid这个Cookie
    }

}
