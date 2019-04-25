package com.mmall.service.impl;

import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 描述：收货地址业务实现类
 * 作者：NearJC
 * 时间：2019.4.25
 */
@Service("shippingService")
public class ShippingService implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 添加收货地址
     *
     * @param userId
     * @param shipping
     * @return
     */
    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        // 判断收货人是否为空
        if (StringUtils.isBlank(shipping.getReceiverName())) {
            return ServerResponse.createByErrorMessage("收货人名称不允许为空");
        }

        // 判断省份是否为空
        if (StringUtils.isBlank(shipping.getReceiverProvince())) {
            return ServerResponse.createByErrorMessage("所在省份不允许为空");
        }

        // 判断城市是否为空
        if (StringUtils.isBlank(shipping.getReceiverCity())) {
            return ServerResponse.createByErrorMessage("所在城市不允许为空");
        }

        // 判断收货人是否为空
        if (StringUtils.isBlank(shipping.getReceiverName())) {
            return ServerResponse.createByErrorMessage("收货人名称不允许为空");
        }

        // 判断收货地址是否为空
        if (StringUtils.isBlank(shipping.getReceiverAddress())) {
            return ServerResponse.createByErrorMessage("收货地址不允许为空");
        }

        // 判断收件人手机是否为空
        if (StringUtils.isBlank(shipping.getReceiverMobile())) {
            return ServerResponse.createByErrorMessage("收件人手机不允许为空");
        }

        // 添加收货地址
        shipping.setUserId(userId);
        int insertCount = shippingMapper.insert(shipping);
        if (insertCount > 0) {
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("添加收货地址成功", result);
        }

        return ServerResponse.createByErrorMessage("添加收货地址失败");
    }

    /**
     * 删除收货地址
     *
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse delete(Integer userId, Integer shippingId) {
        if (userId == null || shippingId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        int resultCount = shippingMapper.deleteByPrimaryKeyAndUserId(userId, shippingId);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }

        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        // 判断收货人是否为空
        if (StringUtils.isBlank(shipping.getReceiverName())) {
            return ServerResponse.createByErrorMessage("收货人名称不允许为空");
        }

        // 判断省份是否为空
        if (StringUtils.isBlank(shipping.getReceiverProvince())) {
            return ServerResponse.createByErrorMessage("所在省份不允许为空");
        }

        // 判断城市是否为空
        if (StringUtils.isBlank(shipping.getReceiverCity())) {
            return ServerResponse.createByErrorMessage("所在城市不允许为空");
        }

        // 判断收货人是否为空
        if (StringUtils.isBlank(shipping.getReceiverName())) {
            return ServerResponse.createByErrorMessage("收货人名称不允许为空");
        }

        // 判断收货地址是否为空
        if (StringUtils.isBlank(shipping.getReceiverAddress())) {
            return ServerResponse.createByErrorMessage("收货地址不允许为空");
        }

        // 判断收件人手机是否为空
        if (StringUtils.isBlank(shipping.getReceiverMobile())) {
            return ServerResponse.createByErrorMessage("收件人手机不允许为空");
        }

        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByShopping(shipping);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("更新地址成功");
        }

        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    /**
     * 获取收货地址详情
     *
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse<Shipping> detail(Integer userId, Integer shippingId) {
        if (userId == null || shippingId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        Shipping shipping = shippingMapper.selectByPrimaryKeyAndUserId(shippingId, userId);

        if (shipping == null) {
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }

        return ServerResponse.createBySuccess("查询成功", shipping);
    }
}
