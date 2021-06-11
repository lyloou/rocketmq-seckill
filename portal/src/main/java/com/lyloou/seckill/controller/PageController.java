package com.lyloou.seckill.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
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

    @ApiOperation("等待支付页面")
    @GetMapping("/watingpay")
    public String watingpay() {
        return "watingpay";
    }

    @ApiOperation("调用支付")
    @GetMapping("/pay")
    public String pay() {
        return "pay";
    }
}
