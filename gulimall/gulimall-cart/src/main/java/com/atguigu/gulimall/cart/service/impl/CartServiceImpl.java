package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.GulimallCartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    ThreadPoolExecutor threadPool;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCurrentUserCartHashOps();
        Object o = cartOps.get(skuId.toString());
        if (o != null) {
            // 购物车里有该商品，更改数量即可
            String cartItemJSON = o.toString();
            CartItem cartItem = JSON.parseObject(cartItemJSON, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            // 购物车里没有该商品 添加新商品到购物车
            CartItem cartItem = new CartItem();

            //1、远程查询当前要添加的商品的信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            }, threadPool);

            //2、远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, threadPool);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
            String s = JSON.toJSONString(cartItem);

            cartOps.put(skuId.toString(), s);
            return cartItem;
        }
    }


    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfoTo userInfoTo = GulimallCartInterceptor.THREAD_LOCAL.get();
        Long userId = userInfoTo.getUserId();
        String userKey = userInfoTo.getUserKey();

        Cart cart = new Cart();

        if (userId != null) {
            // 如果用户有离线购物车，需要合并离线购物车到登录购物车，并且清空临时购物车
            List<CartItem> tempCartItems = listCartItems(userKey);
            if (null != tempCartItems && tempCartItems.size() > 0) {
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount()); // 合并离线购物车的购物项到登录购物车
                }
//                CompletableFuture.runAsync(() -> {
//                    stringRedisTemplate.delete(CartConstant.CART_REDIS_KEY_PREFIX + userKey); // 清空临时购物车
//                }, threadPool);
                clearCart(userKey); // 清空临时购物车
            }
            // 登录了，展示登录购物车
            List<CartItem> loginCartItems = listCartItems(userId);
            cart.setItems(loginCartItems);
        } else {
            // 没登陆，展示离线购物车
            List<CartItem> tempCartItems = listCartItems(userKey);
            cart.setItems(tempCartItems);
        }

        return cart;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCurrentUserCartHashOps();
        String str = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    @Override
    public void clearCart(String cartkey) {
        CompletableFuture.runAsync(() -> {
            stringRedisTemplate.delete(CartConstant.CART_REDIS_KEY_PREFIX + cartkey);
        }, threadPool);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCurrentUserCartHashOps();
        CartItem cartItem = getCartItem(skuId);
//        cartItem.setCheck(check == 1 ? true : false);
        cartItem.setCheck(check == 1);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCurrentUserCartHashOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCurrentUserCartHashOps();
        CompletableFuture.runAsync(() -> {
            cartOps.delete(skuId.toString());
        }, threadPool);
    }

    @Override
    public List<CartItem> getUserCartItems() {
        return null;
    }

    private BoundHashOperations<String, Object, Object> getCurrentUserCartHashOps() {
        UserInfoTo userInfoTo = GulimallCartInterceptor.THREAD_LOCAL.get();
        String userKey = userInfoTo.getUserKey();
        Long userId = userInfoTo.getUserId();
        BoundHashOperations<String, Object, Object> hashOperations;
        if (userId != null) { // 登录了，操作登录购物车
            hashOperations = stringRedisTemplate.boundHashOps(CartConstant.CART_REDIS_KEY_PREFIX + userId);
        } else { // 没登陆，操作离线购物车
            hashOperations = stringRedisTemplate.boundHashOps(CartConstant.CART_REDIS_KEY_PREFIX + userKey);
        }
        return hashOperations;
    }

    private List<CartItem> listCartItems(Object userInfoKey) {
        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(CartConstant.CART_REDIS_KEY_PREFIX + userInfoKey);
        List<Object> values = ops.values();
        if (null != values && values.size() > 0)
            return values.stream().map(item -> JSON.parseObject(item.toString(), CartItem.class)).collect(Collectors.toList());
        return null;
    }
}
