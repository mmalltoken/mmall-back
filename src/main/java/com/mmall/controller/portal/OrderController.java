package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Order;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

/**
 * 描述：订单模块
 * 作者：NearJC
 * 时间：2019.4.28
 */
@Controller
@RequestMapping("/order/")
@Slf4j
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 创建订单
     *
     * @param shippingId
     * @param request
     * @return
     */
    @RequestMapping(value = "create.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse create(Integer shippingId, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return orderService.createOrder(shippingId, user.getId());
    }

    /**
     * 取消订单
     *
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(Long orderNo, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return orderService.cancelOrder(orderNo, user.getId());
    }

    /**
     * 获取购物车中已勾选的商品信息
     *
     * @param request
     * @return
     */
    @RequestMapping("get_cart_checked_product.do")
    @ResponseBody
    public ServerResponse getCartCheckedProduct(HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return orderService.getCartCheckedProduct(user.getId());
    }

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(Long orderNo, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return orderService.getOrderDetail(orderNo, user.getId());
    }

    /**
     * 订单列表
     *
     * @param pageNum
     * @param pageSize
     * @param request
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return orderService.orderList(user.getId(), pageNum, pageSize);
    }

    /**
     * 订单支付
     *
     * @param orderNo
     * @param request
     * @param request
     * @return
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(Long orderNo, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        // 本地存放图片的临时目录
        String path = request.getSession().getServletContext().getRealPath("upload");

        return orderService.orderPay(orderNo, user.getId(), path);
    }

    /**
     * 支付宝回调接口
     *
     * @param request
     * @return
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        Map<String, String> params = Maps.newHashMap();

        // 获取支付宝请求参数
        Map<String, String[]> requestParams = request.getParameterMap();
        // 解析请求参数
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = iter.next();
            String[] valueArray = requestParams.get(name);
            String value = "";
            for (int i = 0; i < valueArray.length; i++) {
                value = (i == (valueArray.length - 1)) ? valueArray[i] : valueArray[i] + ",";
            }
            params.put(name, value);
        }
        log.info("支付宝回调,sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"),
                params.toString());

        // 支付宝验证回调
        params.remove("sign_type");
        try {
            boolean rsaCheckV2Result = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),
                    "UTF-8", Configs.getSignType());

            if (!rsaCheckV2Result) {
                return ServerResponse.createBySuccessMessage("非法请求，验证不通过，再恶意请求立刻通知网警");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝验证回调异常", e);
        }

        // 回调参数验证
        checkCallbackParams(params);

        // 修改订单状态为已支付状态
        ServerResponse response = orderService.aliCallback(params);
        if (response.isSuccess()) {
            return Const.AlipayCallback.TRADE_SUCCESS;
        }

        return Const.AlipayCallback.TRADE_FINISHED;
    }

    /**
     * 回调参数验证
     *
     * @param params
     */
    private void checkCallbackParams(Map<String, String> params) {
        // 验证订单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        ServerResponse<Order> response = orderService.getOrderDetail(orderNo);
        if (!response.isSuccess())
            throw new RuntimeException("订单号错误");

        Order order = response.getData();
        // 验证金额
        BigDecimal totalAmount = new BigDecimal(params.get("total_amount"));
        if (order.getPayment().compareTo(totalAmount) != 0) {
            throw new RuntimeException("订单支付金额错误");
        }

        // 验证AppId
        String appId = params.get("app_id");
        if (!Configs.getAppid().equals(appId)) {
            throw new RuntimeException("AppId不一致");
        }

    }

    /**
     * 查询订单状态
     *
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse queryOrderPayStatus(Long orderNo, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        ServerResponse response = orderService.queryOrderPayStatus(user.getId(), orderNo);

        if (response.isSuccess()) {
            return ServerResponse.createBySuccess(true);
        }

        return ServerResponse.createBySuccess(false);
    }
}