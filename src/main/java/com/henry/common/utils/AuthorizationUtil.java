package com.henry.common.utils;

import com.henry.dto.AuthorizationDto;
import com.henry.common.constants.AuthorizationConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class AuthorizationUtil {
    // create sign
    public static String createSignAuth(String prefix, String key, String authorization) {
        String base = authorization + prefix + System.currentTimeMillis();
        base = AesUtil.encrypt(base, key);
        String signStr = MD5Util.getMD5((base + key).getBytes());
        return signStr + AuthorizationConstant.AUTH_SPIT_PREFIX + base;
    }

    public static AuthorizationDto checkAuth(String key, String signAuth, int refreshInterval) {
        String[] splitSign = signAuth.split(AuthorizationConstant.AUTH_SPIT_PREFIX);
        if (splitSign.length != AuthorizationConstant.TWO) {
            return null;
        }
        String baseEncryptStr = splitSign[AuthorizationConstant.ONE];
        String signStr = splitSign[AuthorizationConstant.ZERO];
        String baseStr = AesUtil.decrypt(baseEncryptStr, key);
        if (StringUtils.isAnyBlank(signStr, baseEncryptStr, baseStr)) {
            return null;
        }
        String md5SignStrNew = MD5Util.getMD5((baseEncryptStr + key).getBytes());
        if (!signStr.equals(md5SignStrNew)) {
            log.debug("check sign failed:sign not correct baseEncryptStr={},sign={}", baseEncryptStr, signStr);
            return null;
        }
        return checkBaseSign(baseStr, signStr, refreshInterval);
    }

    private static AuthorizationDto checkBaseSign(String baseStr, String signStr, int refreshInterval) {
        String[] unBaseStrSplit = baseStr.split(AuthorizationConstant.AUTH_SPIT_PREFIX);
        if (unBaseStrSplit.length != AuthorizationConstant.TWO) {
            return null;
        }
        String authorization = unBaseStrSplit[AuthorizationConstant.ZERO];
        if (StringUtils.isBlank(authorization)) {
            return null;
        }
        AuthorizationDto authorizationDto = new AuthorizationDto();
        authorizationDto.setAuthorization(authorization);
        Long _signMillis = Long.parseLong(unBaseStrSplit[AuthorizationConstant.ONE]);
        if (System.currentTimeMillis() - _signMillis > refreshInterval) {
            authorizationDto.setShouldRefresh(true);
            // for cookie update
            log.debug("check sign failed:sign timeout baseStr={},sign={}", baseStr, signStr);
            return authorizationDto;
        }
        log.debug("check sign success:authorization={}", authorization);
        return authorizationDto;
    }
}
