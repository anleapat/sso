package com.henry.sso.constants;

import com.henry.common.constants.AuthorizationConstant;

public interface SsoConstant extends AuthorizationConstant {
    String SSO_AUTH = "Authorization";
    String SSO_AUTH_USER = "SSO_AUTH_USER";
    String SSO_AUTH_PREFIX = "SSO_AUTH_PREFIX:";
    // user:user information
    String SSO_USER_INFO_PREFIX = "SSO_USER_INFO_PREFIX:";
    // authorization:user get user session
    String SSO_AUTH_USER_PREFIX = "SSO_AUTH_USER_PREFIX:";
    // user:authorization key, for future sso kick out other
    String SSO_USER_DEVICE_AUTH_PREFIX = "SSO_USER_DEVICE_AUTH_PREFIX:";
    String SSO_AUTH_USER_VEHICLE = "SSO_AUTH_USER_VEHICLE:";
    String SSO_HAS_AUTH = "SSO_HAS_AUTH";
    int ZERO = 0;
    int ONE = 1;
    int TWO = 2;
    int THOUSAND = 1000;
    String SSO_CAPTCHA_CODE_PREFIX = "SSO_CAPTCHA_CODE_PREFIX:";
    String SSO_PWD_ERROR_COUNT_PREFIX = "SSO_PWD_ERROR_COUNT_PREFIX:";
    String SSO_LOGIN_FAIL_LOCK_PREFIX = "SSO_LOGIN_FAIL_LOCK_PREFIX:";
}

