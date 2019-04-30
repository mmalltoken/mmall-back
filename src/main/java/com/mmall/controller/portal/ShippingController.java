package com.mmall.controller.portal;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 描述：收货地址管理
 * 作者：NearJC
 * 时间：2019.4.25
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService shippingService;

    /**
     * 添加收货地址
     *
     * @param shipping
     * @param request
     * @return
     */
    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add(Shipping shipping, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return shippingService.add(user.getId(), shipping);
    }

    /**
     * 删除收货地址
     *
     * @param shippingId
     * @param request
     * @return
     */
    @RequestMapping(value = "delete.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse delete(Integer shippingId, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return shippingService.delete(user.getId(), shippingId);
    }

    /**
     * 更新收货地址
     *
     * @param shipping
     * @param request
     * @return
     */
    @RequestMapping(value = "update.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse update(Shipping shipping, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return shippingService.update(user.getId(), shipping);
    }

    /**
     * 获取收获地址详情
     *
     * @param shippingId
     * @param request
     * @return
     */
    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse detail(Integer shippingId, HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return shippingService.detail(user.getId(), shippingId);
    }

    /**
     * 分页查询收货地址列表
     *
     * @param pageNum
     * @param pageSize
     * @param request
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               HttpServletRequest request) {
        // 获取用户信息
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(CookieUtil.readLoginToken(request)),User.class);

        return shippingService.list(user.getId(), pageNum, pageSize);
    }

}
