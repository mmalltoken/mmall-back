package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 描述：Cookie工具类
 * 作者：NearJC
 * 时间：2019.4.30
 */
@Slf4j
public class CookieUtil {

    public static String COOKIE_NAME = "lzy_login_token";
    private static String COOKIE_DOMAIN = ".lzy.com";

    /**
     * 将token存到cookie对象并响应给浏览器
     *
     * @param response
     * @param token
     */
    public static void writeLoginToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        //cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365);
        cookie.setHttpOnly(true);
        log.info("write login token : cookieName = {},cookieValue = {}", cookie.getName(), cookie.getValue());

        response.addCookie(cookie);
    }

    /**
     * 获取cookie中的token
     *
     * @param request
     * @return
     */
    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equals(cookie.getName(), COOKIE_NAME)) {
                    log.info("read login token:cookieName = {},cookieValue = {}", cookie.getName(), cookie.getValue());
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 删除cookie中的token
     *
     * @param request
     * @param response
     */
    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie)) {
                cookie.setDomain(COOKIE_DOMAIN);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                log.info("delete login token: cookieName = {},cookieValue={}", cookie.getName(), cookie.getValue());
                response.addCookie(cookie);
                break;
            }
        }
    }

}
