package com.henry.sso.utils;

import com.google.gson.Gson;
import com.henry.common.constants.AuthorizationConstant;
import com.henry.common.redis.utils.RedisUtil;
import com.henry.common.utils.AuthorizationUtil;
import com.henry.common.utils.CookieUtil;
import com.henry.common.utils.DeviceUtil;
import com.henry.dto.AuthorizationDto;
import com.henry.dto.SsoUserDto;
import com.henry.sso.config.SsoConfig;
import com.henry.sso.constants.SsoConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SessionUtil {
    private static final Gson gson = new Gson();
    @Autowired
    SsoConfig ssoConfig;

    @Autowired
    RedisUtil redisUtil;

    public SsoConfig getSsoConfig() {
        return ssoConfig;
    }

    public boolean isPublicUrl(String url) {
        if (StringUtils.isNotBlank(ssoConfig.getPublicUrls())) {
            String[] publicUrls = ssoConfig.getPublicUrls().split(SsoConstant.SPLIT);
            for (String pubUrl : publicUrls) {
                if (url.contains(AuthorizationConstant.FORWARD_SLASH + pubUrl)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUserLogin() {
        return getLoginUser() != null;
    }

    /**
     * get authorization
     *
     * @return
     */
    public String getAuth() {
        String authorization = (String) getRequest().getAttribute(SsoConstant.SSO_AUTH);
        if (StringUtils.isNotBlank(authorization)) {
            log.debug("use session auth>{}", authorization);
            return authorization;
        }
        boolean isNewAuthorization = true;
        String encAuthorization = CookieUtil.getCookie(getRequest(), SsoConstant.SSO_AUTH);
        if (StringUtils.isNotEmpty(encAuthorization)) {
            authorization = getAuthorization(encAuthorization);
            log.debug("use cookie auth>{}", authorization);
            isNewAuthorization = StringUtils.isEmpty(authorization);
        }
        if (isNewAuthorization) {
            // create authorization
            authorization = UUID.randomUUID().toString();
            log.debug("use new auth>{}", authorization);
            createAuth(authorization);
        }
        getRequest().setAttribute(SsoConstant.SSO_AUTH, authorization);
        return authorization;
    }

    private String getAuthorization(String encAuthorization) {
        String authorization = null;
        AuthorizationDto authorizationDto = AuthorizationUtil.checkAuth(ssoConfig.getAesKey(), encAuthorization, ssoConfig.getRefreshInterval() * SsoConstant.THOUSAND);
        if (authorizationDto != null) {
            if (hasAuth(authorizationDto.getAuthorization())) {
                if (authorizationDto.isShouldRefresh()) {
                    // cache user info in request thread
                    getLoginUser(authorizationDto.getAuthorization());
                    redisUtil.delete(SsoConstant.SSO_AUTH_USER_PREFIX + authorizationDto.getAuthorization());
                    getRequest().removeAttribute(SSO_IS_REFRESH);
                } else {
                    authorization = authorizationDto.getAuthorization();
                }
            }
        }
        return authorization;
    }

    public boolean checkInternalAuth() {
        String internalAuth = getRequest().getHeader(AuthorizationConstant.INTERNAL_AUTH_PREFIX);
        if (StringUtils.isNotBlank(internalAuth)) {
            return true;
        }
        return false;
    }

    private void createAuth(String authorization) {
        // gen UUID
        getRequest().setAttribute(SSO_IS_REFRESH, true);
        HttpServletResponse response = getResponse();
        String domain = ssoConfig.getCookieDomain();
        int timeout = ssoConfig.getLoginTimeout();
        // create sign
        String sign = AuthorizationUtil.createSignAuth(SsoConstant.AUTH_SPIT_PREFIX, ssoConfig.getAesKey(), authorization);
        SsoUserDto _user = (SsoUserDto) getRequest().getAttribute(SsoConstant.SSO_AUTH_USER);
        if (_user != null) {
            setLoginUser(authorization, _user);
        }
        log.debug("refresh authorization>{},sign>{}", authorization, sign);
        // set Cookie
        CookieUtil.setCookie(response, SsoConstant.SSO_AUTH, sign, timeout, domain);
        getRequest().setAttribute(SsoConstant.SSO_AUTH, authorization);
    }

    private final String SSO_IS_REFRESH = "SSO_IS_REFRESH";

    public void refreshAuth(String authorization) {
        // same request refresh authorization once
        if (getRequest().getAttribute(SSO_IS_REFRESH) != null) {
            return;
        }
        // below specify time not need refresh
        long expire = redisUtil.getExpire(SsoConstant.SSO_AUTH_USER_PREFIX + authorization);
        SsoUserDto ssoUserDto = getLoginUser();
        if (expire > ssoConfig.getRefreshInterval() || ssoUserDto == null) {
            // normal authorization let it alone
            return;
        } else {
            // user login clean
            cleanAuth();
            authorization = getAuth();
        }
        // then refresh
        setLoginUser(authorization, ssoUserDto);
        refreshUserVehicle();
        log.debug("refresh authorization:{}", authorization);
        // same request refresh authorization once
        getRequest().setAttribute(SSO_IS_REFRESH, true);
    }

    public String getAuthObject(String prefix) {
        try {
            String sessionPrefix = prefix + getAuth();
            String result = redisUtil.get(sessionPrefix);
            return result;
        } catch (Exception ex) {
            log.error("get authorization error", ex);
            return null;
        }
    }

    public void setAuthObject(String prefix, String val) {
        try {
            String sessionPrefix = prefix + getAuth();
            log.info("sessionPrefix>{}, code>{}", sessionPrefix, val);
            redisUtil.set(sessionPrefix, val, ssoConfig.getCaptchaTimeout());
        } catch (Exception ex) {
            log.error("get authorization error", ex);
        }
    }

    public Object cleanAuthObject(String prefix) {
        try {
            String sessionPrefix = prefix + getAuth();
            return redisUtil.delete(sessionPrefix);
        } catch (Exception ex) {
            log.error("get authorization error", ex);
            return null;
        }
    }

    private boolean hasAuth(String authorization) {
        if (getRequest().getAttribute(SsoConstant.SSO_HAS_AUTH) != null) {
            return (boolean) getRequest().getAttribute(SsoConstant.SSO_HAS_AUTH);
        }
        List<String> results = redisUtil.scan(SsoConstant.STAR + authorization + SsoConstant.STAR);
        boolean result = CollectionUtils.isNotEmpty(results);
        getRequest().setAttribute(SsoConstant.SSO_HAS_AUTH, result);
        return result;
    }

    /**
     * set login user
     *
     * @param user
     * @throws Exception
     */
    public void setLoginUser(SsoUserDto user) {
        setLoginUser(getAuth(), user);
    }

    public void setLoginUser(String authorization, SsoUserDto user) {
        if (user == null || user.getUserId() == null) {
            throw new RuntimeException("UserId can't be empty");
        }
        log.info("user login: authorization:{} username={},userId={}", authorization, user.getUserName(), user.getUserId());
        // sso kick out authorization
        boolean isSingleLogin = ssoConfig.isSinglePointLogin();
        String currentDevice = DeviceUtil.getDeviceType(getRequest());
        if (isSingleLogin) {
            String scanKey = SsoConstant.SSO_USER_DEVICE_AUTH_PREFIX + SsoConstant.STAR + SsoConstant.COLON + user.getUserId();
            List<String> loginDevices = redisUtil.scan(scanKey);
            loginDevices.forEach(loginDeviceKey -> {
                String cacheSessionId = redisUtil.get(loginDeviceKey);
                if (!cacheSessionId.equals(authorization)) {
                    // kick out other device
                    String userKey = redisUtil.get(SsoConstant.SSO_AUTH_USER_PREFIX + cacheSessionId);
                    if (StringUtils.isNotBlank(userKey)) {
                        redisUtil.delete(userKey);
                    }
                    redisUtil.delete(SsoConstant.SSO_AUTH_USER_PREFIX + cacheSessionId);
                    redisUtil.delete(loginDeviceKey);
                }
            });
            log.debug("loginDevices>{}", loginDevices);
        }
        int loginTimeout = ssoConfig.getLoginTimeout();
        // set user (Integer)
        String userKey = SsoConstant.SSO_USER_INFO_PREFIX + currentDevice + SsoConstant.COLON + user.getUserId();
        redisUtil.set(userKey, gson.toJson(user), loginTimeout);
        // bind userKey with new authorization
        redisUtil.set(SsoConstant.SSO_AUTH_USER_PREFIX + authorization, userKey, loginTimeout);
        // bind user + device to authorization, this is used to kick out other session
        String userDeviceSession = SsoConstant.SSO_USER_DEVICE_AUTH_PREFIX + currentDevice + SsoConstant.COLON + user.getUserId();
        redisUtil.set(userDeviceSession, authorization, loginTimeout);
        getRequest().setAttribute(SsoConstant.SSO_AUTH_USER, user);
    }

    public void removeUserVehicle() {
        String key = getUserVehicleKey();
        if (StringUtils.isNotBlank(key)) {
            redisUtil.delete(key);
        }
    }

    public void refreshUserVehicle() {
        String key = getUserVehicleKey();
        int loginTimeout = ssoConfig.getLoginTimeout();
        redisUtil.expire(key, loginTimeout, TimeUnit.SECONDS);
    }

    private String getUserVehicleKey() {
        SsoUserDto ssoUserDto = getLoginUser();
        if (ssoUserDto == null) {
            return null;
        }
        String key = SsoConstant.SSO_AUTH_USER_VEHICLE + ssoUserDto.getUserId();
        return key;
    }

    public void cleanAuth() {
        String authorization = getAuth();
        String userKey = redisUtil.get(SsoConstant.SSO_AUTH_USER_PREFIX + authorization);
        if (userKey != null) {
            redisUtil.delete(userKey);
            String currentDevice = DeviceUtil.getDeviceType(getRequest());
            SsoUserDto ssoUserDto = getLoginUser();
            String userDeviceSession = SsoConstant.SSO_USER_DEVICE_AUTH_PREFIX + currentDevice + SsoConstant.COLON + ssoUserDto.getUserId();
            redisUtil.delete(userDeviceSession);
        }
        redisUtil.delete(SsoConstant.SSO_AUTH_USER_PREFIX + authorization);
        getRequest().removeAttribute(SsoConstant.SSO_AUTH_USER);
        getRequest().removeAttribute(SsoConstant.SSO_AUTH);
        getRequest().removeAttribute(SSO_IS_REFRESH);
        getRequest().getSession().invalidate();
    }

    /**
     * get login user
     */
    public SsoUserDto getLoginUser() {
        SsoUserDto _user = (SsoUserDto) getRequest().getAttribute(SsoConstant.SSO_AUTH_USER);
        if (_user != null) {
            return _user;
        }
        String authorization = getAuth();
        return getLoginUser(authorization);
    }

    public SsoUserDto getLoginUser(String authorization) {
        SsoUserDto _user = (SsoUserDto) getRequest().getAttribute(SsoConstant.SSO_AUTH_USER);
        if (_user != null) {
            return _user;
        }
        if (StringUtils.isEmpty(authorization)) {
            return null;
        }
        String userKey = redisUtil.get(SsoConstant.SSO_AUTH_USER_PREFIX + authorization);
        if (userKey == null) {
            return null;
        }
        String userJson = redisUtil.get(userKey);
        if (userJson == null) {
            return null;
        }
        _user = gson.fromJson(userJson, SsoUserDto.class);
        getRequest().setAttribute(SsoConstant.SSO_AUTH_USER, _user);
        return _user;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        return request;
    }

    private HttpServletResponse getResponse() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletResponse response = attr.getResponse();
        return response;
    }
}
