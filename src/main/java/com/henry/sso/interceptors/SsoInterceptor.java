package com.henry.sso.interceptors;

import com.henry.common.utils.HttpUtil;
import com.henry.sso.utils.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class SsoInterceptor implements HandlerInterceptor {
    @Autowired
    SessionUtil sessionUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // check if in blacklist
        String applicationPath = request.getContextPath();
        String requestPath = request.getRequestURI().substring(applicationPath.length());
        log.info("requestPath>{}", requestPath);
        if (sessionUtil.isPublicUrl(requestPath)) {
            return true;
        }

        String sessionId = sessionUtil.getAuth();
        if (sessionId == null) {
            HttpUtil.writeUnLogin(response);
            return false;
        }
        // refresh session
        sessionUtil.refreshAuth(sessionId);
        if (!sessionUtil.isUserLogin()) {
            log.error("Not signed in. sessionId={}", sessionId);
            HttpUtil.writeUnLogin(response);
            return false;
        }
        sessionUtil.refreshUserVehicle();
        return true;
    }
}
