package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 描述：购物车业务实现类
 * 作者：NearJC
 * 时间：2019.4.27
 */
@Service("cartService")
public class CartService implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加商品
     *
     * @param productId
     * @param count
     * @return
     */
    @Override
    public ServerResponse<CartVo> addCartProduct(Integer productId, Integer userId, Integer count) {
        // 参数校验
        if (productId == null || count == null) {
            return ServerResponse.createByErrorMessage("添加商品参数错误");
        }

        // 查询商品
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("添加失败，没有该商品");
        }

        // 查询用户在购物车中是否有该商品
        Cart cart = cartMapper.selectByProductIdAndUserId(productId, userId);
        if (cart == null) { // 将商品添加到商品中
            Cart cartItem = new Cart();
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setUserId(userId);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cartItem);
        } else {   // 更改购物车中的商品数量
            count = count + cart.getQuantity();
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }

        // 封装购物车数据
        CartVo cartVo = assembleCartVo(userId);

        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 购物车Vo
     *
     * @param userId
     * @return
     */
    private CartVo assembleCartVo(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectByUserId(userId);

        BigDecimal totalPrice = new BigDecimal("0");
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        for (Cart cartItem : cartList) {
            CartProductVo cartProductVo = new CartProductVo();

            // 查询商品信息
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            cartProductVo.setId(cartItem.getId());
            cartProductVo.setProductId(cartItem.getProductId());
            cartProductVo.setProductChecked(cartItem.getChecked());
            cartProductVo.setUserId(userId);
            cartProductVo.setProductName(product.getName());
            cartProductVo.setProductSubtitle(product.getSubtitle());
            cartProductVo.setProductMainImage(product.getMainImage());
            cartProductVo.setProductPrice(product.getPrice());
            cartProductVo.setProductStock(product.getStock());
            cartProductVo.setProductStatus(product.getStatus());

            // 库存不足时，限制购买数量为当前库存数量
            int buyLimitCount = 0;
            if (product.getStock() >= cartItem.getQuantity()) {
                buyLimitCount = cartItem.getQuantity();
                cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);   // 限制成功
            } else {
                buyLimitCount = product.getStock();
                cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);   // 限制失败

                // 更新购物车商品购买数量
                Cart updateCart = new Cart();
                updateCart.setId(cartItem.getId());
                updateCart.setQuantity(product.getStock());
                cartMapper.updateByPrimaryKeySelective(updateCart);
            }
            cartProductVo.setQuantity(buyLimitCount);

            // 计算商品总价
            cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(cartProductVo.getProductPrice().doubleValue(),
                    cartProductVo.getQuantity().doubleValue()));

            // 统计购物车中勾选的总价
            if (cartProductVo.getProductChecked() == Const.Cart.CHECKED) {
                totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
            }

            cartProductVoList.add(cartProductVo);
        }

        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setTotalPrice(totalPrice);
        cartVo.setAllChecked(this.isCheckedAll(userId));

        return cartVo;
    }

    /**
     * 是否全部勾选
     *
     * @param userId
     * @return
     */
    private boolean isCheckedAll(Integer userId) {
        if (userId == null)
            return false;

        return cartMapper.selectCheckedAll(userId) == 0;
    }
}
