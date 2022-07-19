package com.henry.sso.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource("classpath:sso.properties")
@ConfigurationProperties(prefix = "com.henry.sso")
public class SsoConfig {
    private String aesKey;
    private int refreshInterval;
    private int loginTimeout;
    private boolean singlePointLogin;
    private int maxLoginFailedCount;
    private String cookieDomain;
    private String publicUrls;
    private long captchaTimeout;
    private long loginFailLockTime;
}
