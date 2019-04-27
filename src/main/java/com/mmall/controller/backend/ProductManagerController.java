package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 描述：后台-商品管理
 * 作者：NearJC
 * 时间：2019.4.26
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManagerController {

    @Autowired
    private IProductService productService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IFileService fileService;

    /**
     * 新增或更新商品信息
     *
     * @param product
     * @param session
     * @return
     */
    @RequestMapping(value = "save.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> productSave(Product product, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {
            //填充我们增加产品的业务逻辑
            return productService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 修改产品销售状态
     *
     * @param productId
     * @param status
     * @param session
     * @return
     */
    @RequestMapping(value = "set_sale_status.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {
            return productService.setSaleStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 商品详情
     *
     * @param productId
     * @param session
     * @return
     */
    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {
            return productService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 查询商品
     *
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> search(String productName, Integer productId,
                                           @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {
            return productService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param session
     * @return
     */
    @RequestMapping(value = "upload.do")
    @ResponseBody
    public ServerResponse upload(@RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile, HttpServletRequest request,
                                 HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        if (userService.checkAdminRole(user).isSuccess()) {
            // 获取本地临时目录路径
            String path = request.getSession().getServletContext().getRealPath("upload");
            // 上传文件
            String targetFileName = fileService.upload(multipartFile, path);

            // 返回路径为null
            if (StringUtils.isBlank(targetFileName))
                return ServerResponse.createByErrorMessage("文件上传失败");

            Map result = Maps.newHashMap();
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            result.put("uri", targetFileName);
            result.put("url", url);

            return ServerResponse.createBySuccess(result);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping(value = "rich_text_upload.do")
    @ResponseBody
    public Map richTextUpload(@RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        Map result = Maps.newHashMap();

        if (user == null) {
            result.put("success", false);
            result.put("msg", "用户未登录，请登录");
            return result;
        }

        if (userService.checkAdminRole(user).isSuccess()) {
            //获取本地临时目录路径
            String path = request.getSession().getServletContext().getRealPath("upload");
            // 上传文件
            String targetFileName = fileService.upload(multipartFile, path);

            if (StringUtils.isEmpty(targetFileName)) {
                result.put("success", false);
                result.put("msg", "文件上传失败");

                return result;
            }

            // 上传成功
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            result.put("success", true);
            result.put("msg", "文件上传成功");
            result.put("file_path", url);
            // 添加头响应
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
        } else {
            result.put("success", false);
            result.put("msg", "无权限操作");
        }

        return result;
    }
}
