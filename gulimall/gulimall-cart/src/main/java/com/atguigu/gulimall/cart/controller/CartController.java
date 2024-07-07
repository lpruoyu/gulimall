package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getUserCartItems();
    }

    /**
     * 删除该购物项
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 改变购物项数量
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num){
        cartService.changeItemCount(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * 改变购物项选中状态
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 浏览器有一个cookie；user-key；标识用户身份，一个月后过期；
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份；user-key这个Cookie
     * 浏览器以后保存，每次访问都会带上这个cookie；
     * <p>
     * 登录：session有用户信息
     * 没登录：按照cookie里面带来user-key来做
     * 第一次使用购物车页面：如果没有临时用户user-key，帮忙创建一个临时用户user-key。
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
//        Cart cart = cartService.getCart();
//        model.addAttribute("cart",cart);
//        UserInfoTo userInfoTo = GulimallCartInterceptor.THREAD_LOCAL.get();
//        System.out.println("CartController ===> " + userInfoTo);

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * http://cart.gulimall.com/addToCart?skuId=1&num=1
     *
     * RedirectAttributes ra
     *      ra.addFlashAttribute();将数据放在session里面可以在页面取出，但是只能取一次
     *      ra.addAttribute("skuId",skuId);将数据放在url后面
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess"; //重定向到成功页面，防止用户刷新页面再次提交数据添加到购物车
    }

    /**
     * 跳转到成功页
     */
    @GetMapping("/addToCartSuccess")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        //添加商品到购物车成功，再次查询购物车数据即可
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item", item);
        return "success";
    }

}
