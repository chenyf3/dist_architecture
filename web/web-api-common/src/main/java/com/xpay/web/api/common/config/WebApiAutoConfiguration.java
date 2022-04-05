package com.xpay.web.api.common.config;

import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.web.api.common.error.handler.GlobalErrorAttributes;
import com.xpay.web.api.common.error.handler.GlobalExceptionHandler;
import com.xpay.web.api.common.interceptor.AuthorityInterceptor;
import com.xpay.web.api.common.interceptor.TokenInterceptor;
import com.xpay.web.api.common.manager.CodeManager;
import com.xpay.web.api.common.manager.FuncManager;
import com.xpay.web.api.common.manager.TokenManager;
import com.xpay.web.api.common.manager.impl.CodeManagerImpl;
import com.xpay.web.api.common.manager.impl.FuncManagerImpl;
import com.xpay.web.api.common.manager.impl.TokenManagerImpl;
import com.xpay.web.api.common.resolvers.CurrentUserMethodArgumentResolver;
import com.xpay.web.api.common.resolvers.CustomizeHandlerExceptionResolver;
import com.xpay.web.api.common.service.*;
import com.xpay.web.api.common.service.impl.DefaultCryptService;
import com.xpay.web.api.common.service.impl.DefaultDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@ComponentScan(basePackages = "com.xpay.web.api.common")//增加当前模块的包路径扫描
@EnableConfigurationProperties(WebApiProperties.class)
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)//因为 ErrorAttributes 要比ErrorMvcAutoConfiguration先生效
@Configuration
public class WebApiAutoConfiguration {
    @Autowired
    private WebApiProperties properties;
    @Autowired
    private ServerProperties serverProperties;
    @Autowired
    private List<ErrorViewResolver> errorViewResolvers;

    @Bean
    public TokenManager tokenManager(RedisClient redisClient){
        return new TokenManagerImpl(properties, redisClient);
    }

    @Bean
    public FuncManager funcManager(RedisClient redisClient, AuthService authService, UserService userService){
        return new FuncManagerImpl(properties.getAppName(), redisClient, authService, userService);
    }

    @ConditionalOnMissingBean(CodeManager.class)
    @Bean
    public CodeManager codeManager(RedisClient redisClient){
        return new CodeManagerImpl(redisClient);
    }

    @Bean
    public TokenInterceptor tokenInterceptor(TokenManager tokenManager){
        return new TokenInterceptor(tokenManager, properties);
    }

    @Bean
    public AuthorityInterceptor authorityInterceptor(FuncManager funcManager){
        return new AuthorityInterceptor(funcManager, properties);
    }

    @Bean
    public CurrentUserMethodArgumentResolver currentUserMethodArgumentResolver(UserService userService){
        return new CurrentUserMethodArgumentResolver(userService);
    }

    @ConditionalOnMissingBean(CryptService.class)
    @Bean
    public CryptService cryptService(){
        return new DefaultCryptService(properties.getRsaPublicKey(), properties.getRsaPrivateKey());
    }

    @ConditionalOnMissingBean(DictionaryService.class)
    @Bean
    public DictionaryService dictionaryService(){
        return new DefaultDictionaryService();
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public ErrorAttributes errorAttributes() {
        return new GlobalErrorAttributes();
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
    public GlobalExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes){
        return new GlobalExceptionHandler(errorAttributes, this.serverProperties.getError(), this.errorViewResolvers);
    }

    @Bean
    public FilterRegistrationBean crosFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration cros = new CorsConfiguration();
        cros.setAllowCredentials(false);
        String[] origins = properties.getAllowOrigins().split(",");
        String[] methods = properties.getAllowMethods().split(",");
        String[] headers = properties.getAllowHeaders().split(",");
        for(int i=0; i<origins.length; i++){
            cros.addAllowedOrigin(origins[i]);
        }
        for(int i=0; i<methods.length; i++){
            cros.addAllowedMethod(methods[i]);
        }
        for(int i=0; i<headers.length; i++){
            cros.addAllowedHeader(headers[i]);
        }
        cros.setMaxAge(Long.valueOf(properties.getMaxAge()));
        source.registerCorsConfiguration("/**", cros);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public MvcConfig mvcConfig(TokenInterceptor tokenInterceptor,
                               AuthorityInterceptor authorityInterceptor,
                               CurrentUserMethodArgumentResolver argumentResolver){
        return new MvcConfig(tokenInterceptor, authorityInterceptor, argumentResolver);
    }

    class MvcConfig implements WebMvcConfigurer {
        TokenInterceptor tokenInterceptor;
        AuthorityInterceptor authorityInterceptor;
        CurrentUserMethodArgumentResolver argumentResolver;

        public MvcConfig(TokenInterceptor tokenInterceptor,
                AuthorityInterceptor authorityInterceptor,
                CurrentUserMethodArgumentResolver argumentResolver){
            this.tokenInterceptor = tokenInterceptor;
            this.authorityInterceptor = authorityInterceptor;
            this.argumentResolver = argumentResolver;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(tokenInterceptor);
            registry.addInterceptor(authorityInterceptor);
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(argumentResolver);
        }

//        @Override
//        public void addCorsMappings(CorsRegistry registry) {
//            registry.addMapping("/**")
//                    .allowCredentials(properties.getAllowCredentials())
//                    .allowedOrigins(properties.getAllowOrigins().split(","))
//                    .allowedMethods(properties.getAllowMethods().split(","))
//                    .allowedHeaders(properties.getAllowHeaders().split(","))
//                    .maxAge(Long.valueOf(properties.getMaxAge()));
//        }

        @Override
        public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers){
            resolvers.add(new CustomizeHandlerExceptionResolver());
        }
    }
}
