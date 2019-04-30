package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 描述：后台 - 订单模块
 * 作者：NearJC
 * 时间：2019.4.28
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManagerController {

    @Autowired
    private IOrderService orderService;

    /**
     * 订单详情
     *
     * @param orderNo
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> detail(Long orderNo) {
        return orderService.managerDetail(orderNo);
    }

    /**
     * 根据条件查询订单
     *
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> search(Long orderNo,
                                           @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return orderService.manageSearch(orderNo, pageNum, pageSize);
    }

    /**
     * 订单发货
     *
     * @param orderNo
     * @return
     */
    @RequestMapping("deliver_goods.do")
    @ResponseBody
    public ServerResponse<String> deliverGoods(Long orderNo) {
        return orderService.manageDeliverGoods(orderNo);
    }
}
