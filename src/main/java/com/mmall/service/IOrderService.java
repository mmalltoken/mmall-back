package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

public interface IOrderService {

    ServerResponse<OrderVo> createOrder(Integer shippingId, Integer userId);

    ServerResponse<String> cancelOrder(Long orderNo, Integer userId);
}
