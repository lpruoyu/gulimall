package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.GulimallCartInterceptor;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ExecutionException;

@Controller
public class CartController {
//    @Autowired
//    private CartService cartService;

    /**
     * 浏览器有一个cookie；user-key；标识用户身份，一个月后过期；
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份；user-key这个Cookie
     * 浏览器以后保存，每次访问都会带上这个cookie；

     * 登录：session有用户信息
     * 没登录：按照cookie里面带来user-key来做
     * 第一次使用购物车页面：如果没有临时用户user-key，帮忙创建一个临时用户user-key。
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
//        Cart cart = cartService.getCart();
//        model.addAttribute("cart",cart);
        UserInfoTo userInfoTo = GulimallCartInterceptor.THREAD_LOCAL.get();
        System.out.println("CartController ===> " + userInfoTo);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * http://cart.gulimall.com/addToCart?skuId=1&num=1
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num) {
        return "success";
    }

}
