package com.sportzone.config;

import com.sportzone.interceptors.AdminLoginInterceptor;
import com.sportzone.interceptors.UserLoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserLoginInterceptor userLoginInterceptor;
    private final AdminLoginInterceptor adminLoginInterceptor;

    public WebMvcConfig(UserLoginInterceptor userLoginInterceptor, AdminLoginInterceptor adminLoginInterceptor) {
        this.userLoginInterceptor = userLoginInterceptor;
        this.adminLoginInterceptor = adminLoginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userLoginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/register",
                        "/api/user/login",
                        "/api/user/product/**",
                        "/api/user/points/gifts",
                        "/api/user/coupon/list",
                        "/api/admin/**",
                        "/api/public/**");

        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login");
    }
}