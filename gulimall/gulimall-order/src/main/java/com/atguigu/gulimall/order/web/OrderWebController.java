package com.atguigu.gulimall.order.web;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.exception.NoStockException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 订单分页查询
     */
    @GetMapping("/orderList")
    public String orderList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                            Model model, HttpServletRequest request) {
        //查出当前登录的用户的所有订单列表数据
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageNum.toString());
        PageUtils data = orderService.listWithItem(params);
        R r = new R().put("page", data);
        System.out.println(JSON.toJSONString(r));
        model.addAttribute("orders", r);
        return "orderList";
    }

    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        //展示订单确认的数据
        return "confirm";
    }

    /**
     * 下单功能
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("订单提交的数据..." + vo);
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            System.out.println("responseVo : " + responseVo);
            if (responseVo.getCode() == 0) {
                //下单成功来到支付选择页
//                model.addAttribute("submitOrderResp", responseVo);
//                return "pay";

//                支付服务太麻烦,我直接默认支付成功,跳转到list.html
                (((OrderDao) orderService.getBaseMapper())).updateOrderStatus(responseVo.getOrder().getOrderSn(), OrderStatusEnum.PAYED.getCode());
                return "redirect:http://order.gulimall.com/orderList";

            } else {
                //下单失败回到订单确认页重新确认订单信息
                String msg = "下单失败；";
                switch (responseVo.getCode()) {
                    case 1:
                        msg += "订单信息过期，请刷新再次提交";
                        break;
                    case 2:
                        msg += "订单商品价格发生变化，请确认后再次提交";
                        break;
                    case 3:
                        msg += "库存锁定失败，商品库存不足";
                        break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e) {
            if (e instanceof NoStockException) {
                String message = ((NoStockException) e).getMessage();
                redirectAttributes.addFlashAttribute("msg", message);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

}