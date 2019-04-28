package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.CartCheckedProductVo;
import com.mmall.vo.OrderVo;

public interface IOrderService {

    ServerResponse<OrderVo> createOrder(Integer shippingId, Integer userId);

    ServerResponse<String> cancelOrder(Long orderNo, Integer userId);

    ServerResponse<CartCheckedProductVo> getCartCheckedProduct(Integer userId);

    ServerResponse<OrderVo> getOrderDetail(Long orderNo, Integer userId);

    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);

    ServerResponse<OrderVo> managerDetail(Long orderNo);

    ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);

    ServerResponse<String> manageDeliverGoods(Long orderNo);
}
