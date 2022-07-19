# sso
** How to use **

1.add a configuration class

```javascript
@Slf4j
@Configuration
public class ApiInterceptorConfig implements WebMvcConfigurer {
    @Autowired
    SsoInterceptor ssoInterceptor;
    // for spring device
    @Autowired
    DeviceResolverHandlerInterceptor deviceResolverHandlerInterceptor;
    @Autowired
    SsoConfig ssoConfig;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("===set interceptor....");
        registry.addInterceptor(deviceResolverHandlerInterceptor);
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(ssoInterceptor) // sso interceptor
                .addPathPatterns("/**");
        interceptorRegistration.excludePathPatterns("/swagger-ui.html/**").excludePathPatterns("/v2/**")
                .excludePathPatterns("/webjars/**");
        interceptorRegistration.excludePathPatterns("/swagger-resources").excludePathPatterns("/favicon.ico");
        if (StringUtils.isNotBlank(ssoConfig.getPublicUrls())) {
            for (String url : ssoConfig.getPublicUrls().split(SsoConstant.SPLIT)) {
                interceptorRegistration.excludePathPatterns(AuthConstant.FORWARD_SLASH + url);
            }
        }
    }
}


```

2.spring redis configuration

```javascript
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.lettuce.pool.min-idle = 0
spring.redis.lettuce.pool.max-idle = 150
spring.redis.lettuce.pool.max-wait = 120
spring.redis.lettuce.pool.max-active = 150

```
3.watch the youtube turtorial
https://youtu.be/P_E1I1awfRk
