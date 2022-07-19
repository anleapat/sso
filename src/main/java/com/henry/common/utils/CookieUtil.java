package com.henry.common.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CookieUtil {
    /**
     * @param response
     * @param name
     * @param value
     * @param maxAge
     * @param domain
     */
    public static void setCookie(HttpServletResponse response, String name, String value,
                                 int maxAge, String domain) {
        try {
            Cookie cookie = new Cookie(name, URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
            cookie.setPath("/");
            cookie.setMaxAge(maxAge);
            if (StringUtils.isNotEmpty(domain)) {
                cookie.setDomain(domain);
            }
            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * get cookie
     *
     * @param request
     * @param name
     * @return
     */
    public static String getCookie(HttpServletRequest request, String name) {
        String value = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    try {
                        value = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
            }
        }
        return value;
    }

}
