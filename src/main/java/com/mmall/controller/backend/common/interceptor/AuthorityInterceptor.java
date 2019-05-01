package com.mmall.controller.backend.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * 描述：统一权限管理
 * 作者：NearJC
 * 时间：2019.5.1
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取请求方法名
        String methodName = handlerMethod.getMethod().getName();
        // 获取请求类名
        String className = handlerMethod.getBean().getClass().getSimpleName();

        // 解析请求参数
        StringBuilder res = new StringBuilder();
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        Iterator<String> iterator = parameterMap.keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            String[] values = parameterMap.get(name);
            res.append(name).append("=").append(Arrays.toString(values));
        }
        log.info("类名：{}，方法名：{}，参数：{}", className, methodName, res.toString());

        // 获取用户信息
        HttpSession session = httpServletRequest.getSession();
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        // 拦截用户不存在或没有权限时
        if (user == null || user.getRole() != Const.Role.ROLE_ADMIN) {
            Map<String, Object> resultMap = Maps.newHashMap();
            // 重置response
            httpServletResponse.reset();
            // 设置响应编码
            httpServletResponse.setCharacterEncoding("UTF-8");
            // 设置响应内容类型
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            // 获取响应对象
            PrintWriter out = httpServletResponse.getWriter();
            if (user == null) {   // 用户为空时
                if (className.equals("ProductManagerController") && methodName.equals("richTextUpload")) {
                    resultMap.put("success", false);
                    resultMap.put("msg", "用户未登录");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("用户未登录")));
                }
            } else {
                if (className.equals("ProductManagerController.class") && methodName.equals("richTextUpload")) {
                    resultMap.put("success", false);
                    resultMap.put("msg", "权限不足，请登录管理员帐号");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("权限不足，请登录管理员帐号")));
                }
            }
            out.flush();
            out.close();

            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
