package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

public interface ICartService {

    ServerResponse<CartVo> addCartProduct(Integer productId, Integer userId, Integer count);

    ServerResponse<CartVo> updateCartProduct(Integer productId, Integer userId, Integer count);

    ServerResponse<CartVo> deleteCartProduct(String productIds, Integer userId);

    ServerResponse<CartVo> list(Integer userId);

    ServerResponse<CartVo> checkOrUnCheck(Integer userId, Integer productId, int checked);

    ServerResponse<Integer> getCartProductCount(Integer userId);
}
