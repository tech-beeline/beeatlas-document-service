/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.beeline.documentservice.controller.HeaderInterceptor;

@Configuration
class MyWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HeaderInterceptor());
    }
}