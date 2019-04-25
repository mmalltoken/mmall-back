package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManagerController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ICategoryService categoryService;

    /**
     * 添加分类
     *
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do")
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session, String categoryName,
                                              @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        // 判断是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        // 校验是否是管理员身份
        if (userService.checkAdminRole(user).isSuccess()) {  // 是管理员
            return categoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 修改分类名称
     *
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "set_category_name.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        //　判断是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        // 判断是否是管理员身份
        if (userService.checkAdminRole(user).isSuccess()) { // 是管理员
            // 更新分类名称
            return categoryService.updateCategoryName(categoryId, categoryName);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 查询子节点信息
     *
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_children.do")
    @ResponseBody
    public ServerResponse getChildrenNode(HttpSession session, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        //　判断是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        // 判断是否是管理员身份
        if (userService.checkAdminRole(user).isSuccess()) { // 是管理员
            // 查询子节点category信息，并且不递归，保持平级
            return categoryService.getChildrenNode(parentId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
}
