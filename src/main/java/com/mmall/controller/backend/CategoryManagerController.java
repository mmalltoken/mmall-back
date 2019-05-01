package com.mmall.controller.backend;

import com.mmall.common.ServerResponse;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManagerController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 添加分类
     *
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do")
    @ResponseBody
    public ServerResponse<String> addCategory(String categoryName,
                                              @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        return categoryService.addCategory(categoryName, parentId);
    }

    /**
     * 修改分类名称
     *
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "set_category_name.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName) {
        return categoryService.updateCategoryName(categoryId, categoryName);
    }

    /**
     * 查询子节点信息
     *
     * @param parentId
     * @return
     */
    @RequestMapping(value = "get_children.do")
    @ResponseBody
    public ServerResponse getChildrenNode(@RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        return categoryService.getChildrenNode(parentId);
    }

    /**
     * 查询本身及子节点的id
     *
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_self_children_id.do")
    @ResponseBody
    public ServerResponse getSelfAndChildrenId(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        return categoryService.getSelfAndChildrenId(categoryId);
    }
}
