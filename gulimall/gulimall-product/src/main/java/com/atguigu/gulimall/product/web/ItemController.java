package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {
    @Autowired
    private SkuInfoService skuInfoService;

//    测试
    @GetMapping("/shangpinxiangqing.html")
    public String itemPage() {
        return "shangpinxiangqing";
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 展示当前sku的详情
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model, HttpServletRequest httpServletRequest, HttpSession session) throws ExecutionException, InterruptedException {
        System.out.println("准备查询"+skuId+"详情");
        SkuItemVo vo = skuInfoService.item(skuId);
        model.addAttribute("item",vo);

//        Cookie[] cookies = httpServletRequest.getCookies();
//        if (null != cookies && cookies.length > 0) {
//            for (Cookie cookie : cookies) {
//                if (cookie.getName().equals(AuthServerConstant.REDIS_SESSION_ID_KEY)) {
//                    String rsessionId = cookie.getValue();
//                    String rsessionJson = stringRedisTemplate.opsForValue().get(rsessionId);
//                    HashMap<String, String> rsession;
//                    if (StringUtils.isEmpty(rsessionJson)) {
//                        rsession = new HashMap<>();
//                    } else {
//                        rsession = JSON.parseObject(rsessionJson, HashMap.class);
//                    }
//                    String s = rsession.get(AuthServerConstant.LOGIN_USER);
//                    if (!StringUtils.isEmpty(s)) {
//                        MemberRespVo loginUser = JSON.parseObject(s, new TypeReference<MemberRespVo>() {
//                        });
//                        session.setAttribute(AuthServerConstant.LOGIN_USER, loginUser);
//                    }
//                }
//            }
//        }

        return "item";
    }
}
