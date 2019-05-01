package com.mmall.controller.portal.common.interceptor;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * 描述：前台统一权限管理
 * 作者:NearJC
 * 时间：2019.5.1
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取请求类名
        String className = handlerMethod.getBean().getClass().getSimpleName();
        // 获取请求方法名
        String methodName = handlerMethod.getMethod().getName();

        // 解析请求参数
        StringBuilder res = new StringBuilder();
        Map<String, String[]> parameterMap = request.getParameterMap();
        Iterator<String> it = parameterMap.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            String[] values = parameterMap.get(name);
            res.append(name).append("=").append(values);
        }
        log.info("类名：{}，方法名：{}，参数：{}", className, methodName, res.toString());

        // 获取用户
        String loginToken = CookieUtil.readLoginToken(request);
        User user = JsonUtil.string2Obj(RedisShardedUtil.get(loginToken), User.class);

        // 判断用户是否登录
        if (user == null) {
            // 重置响应对象
            response.reset();
            // 设置响应编码
            response.setContentType("UTF-8");
            // 设置响应类型
            response.setContentType("application/json;charset=UTF-8");
            // 获取输出流对象
            PrintWriter out = response.getWriter();
            // 设置响应信息
            out.print(JsonUtil.obj2String(ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    "用户未登录")));
            // 刷新输出流
            out.flush();
            // 关闭流
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
