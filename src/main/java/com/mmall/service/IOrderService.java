package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Order;
import com.mmall.vo.CartCheckedProductVo;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {

    ServerResponse<OrderVo> createOrder(Integer shippingId, Integer userId);

    ServerResponse<String> cancelOrder(Long orderNo, Integer userId);

    ServerResponse<CartCheckedProductVo> getCartCheckedProduct(Integer userId);

    ServerResponse<OrderVo> getOrderDetail(Long orderNo, Integer userId);

    ServerResponse<Order> getOrderDetail(Long orderNo);

    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);

    ServerResponse orderPay(Long orderNo, Integer userId, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);

    ServerResponse<OrderVo> managerDetail(Long orderNo);

    ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);

    ServerResponse<String> manageDeliverGoods(Long orderNo);

}
