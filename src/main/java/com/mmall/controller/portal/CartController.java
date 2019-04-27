package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Cart;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 描述：前台-购物车模块
 * 作者：NearJC
 * 时间：2019.4.27
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService cartService;

    /**
     * 将商品添加到购物车
     *
     * @param productId
     * @param count
     * @param session
     * @return
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartVo> add(Integer productId, Integer count, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return cartService.addCartProduct(productId, user.getId(), count);
    }

    /**
     * 更新购物车商品数量
     *
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<CartVo> update(Integer count, Integer productId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return cartService.updateCartProduct(productId, user.getId(), count);
    }

    /**
     * 删除购物车中的商品
     *
     * @param productIds
     * @param session
     * @return
     */
    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse<CartVo> delete(String productIds, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return cartService.deleteCartProduct(productIds, user.getId());
    }

    /**
     * 查询购物车列表
     *
     * @param session
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return cartService.list(user.getId());
    }

    /**
     * 全部勾选
     *
     * @param session
     * @return
     */
    @RequestMapping("check_all.do")
    @ResponseBody
    public ServerResponse<CartVo> checkAll(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return cartService.checkOrUnCheck(user.getId(), null, Const.Cart.CHECKED);
    }

    /**
     * 全部反选
     *
     * @param session
     * @return
     */
    @RequestMapping("uncheck_all.do")
    @ResponseBody
    public ServerResponse<CartVo> uncheckAll(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return cartService.checkOrUnCheck(user.getId(), null, Const.Cart.UN_CHECKED);
    }

    /**
     * 勾选单个商品
     *
     * @param productId
     * @param session
     * @return
     */
    @RequestMapping("check.do")
    @ResponseBody
    public ServerResponse<CartVo> check(Integer productId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return cartService.checkOrUnCheck(user.getId(), productId, Const.Cart.CHECKED);
    }

    /**
     * 勾选单个商品
     *
     * @param productId
     * @param session
     * @return
     */
    @RequestMapping("uncheck.do")
    @ResponseBody
    public ServerResponse<CartVo> uncheck(Integer productId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        return cartService.checkOrUnCheck(user.getId(), productId, Const.Cart.UN_CHECKED);
    }

    /**
     * 获取购买车中的商品数量
     *
     * @param session
     * @return
     */
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServerResponse.createBySuccess(0);
        }

        return cartService.getCartProductCount(user.getId());
    }

}
