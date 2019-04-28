package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 描述：订单模块
 * 作者：NearJC
 * 时间：2019.4.28
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 创建订单
     *
     * @param shippingId
     * @param session
     * @return
     */
    @RequestMapping(value = "create.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<OrderVo> create(Integer shippingId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return orderService.createOrder(shippingId, user.getId());
    }

    /**
     * 取消订单
     *
     * @param orderNo
     * @param session
     * @return
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse<String> cancel(Long orderNo, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return orderService.cancelOrder(orderNo, user.getId());
    }

}
