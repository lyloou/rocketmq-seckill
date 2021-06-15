package com.lyloou.seckill.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Controller
public class PageController {
    @ApiOperation("订单页面")
    @GetMapping("/order")
    public String order() {
        return "order";
    }

    @ApiOperation("调用支付")
    @GetMapping("/pay")
    public String pay(String orderNo, Model model) {
        model.addAttribute("orderNo", orderNo);
        return "pay";
    }
}
