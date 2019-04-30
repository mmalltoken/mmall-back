package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

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
     * @param request
     * @return
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(Integer productId, Integer count, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return cartService.addCartProduct(productId, user.getId(), count);
    }

    /**
     * 更新购物车商品数量
     *
     * @param count
     * @param productId
     * @param request
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(Integer count, Integer productId, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return cartService.updateCartProduct(productId, user.getId(), count);
    }

    /**
     * 删除购物车中的商品
     *
     * @param productIds
     * @param request
     * @return
     */
    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse delete(String productIds, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return cartService.deleteCartProduct(productIds, user.getId());
    }

    /**
     * 查询购物车列表
     *
     * @param request
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return cartService.list(user.getId());
    }

    /**
     * 全部勾选
     *
     * @param request
     * @return
     */
    @RequestMapping("check_all.do")
    @ResponseBody
    public ServerResponse checkAll(HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return cartService.checkOrUnCheck(user.getId(), null, Const.Cart.CHECKED);
    }

    /**
     * 全部反选
     *
     * @param request
     * @return
     */
    @RequestMapping("uncheck_all.do")
    @ResponseBody
    public ServerResponse uncheckAll(HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return cartService.checkOrUnCheck(user.getId(), null, Const.Cart.UN_CHECKED);
    }

    /**
     * 勾选单个商品
     *
     * @param productId
     * @param request
     * @return
     */
    @RequestMapping("check.do")
    @ResponseBody
    public ServerResponse check(Integer productId, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return cartService.checkOrUnCheck(user.getId(), productId, Const.Cart.CHECKED);
    }

    /**
     * 勾选单个商品
     *
     * @param productId
     * @param request
     * @return
     */
    @RequestMapping("uncheck.do")
    @ResponseBody
    public ServerResponse uncheck(Integer productId, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return cartService.checkOrUnCheck(user.getId(), productId, Const.Cart.UN_CHECKED);
    }

    /**
     * 获取购买车中的商品数量
     *
     * @param request
     * @return
     */
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);
        if(user == null){
            return ServerResponse.createBySuccess(0);
        }

        return cartService.getCartProductCount(user.getId());
    }

}
