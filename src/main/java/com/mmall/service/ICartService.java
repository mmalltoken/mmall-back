package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

public interface ICartService {

    ServerResponse<CartVo> addCartProduct(Integer productId, Integer userId, Integer count);

    ServerResponse<CartVo> updateCartProduct(Integer productId, Integer userId, Integer count);
}
