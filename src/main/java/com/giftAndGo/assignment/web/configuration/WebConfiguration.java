package com.giftAndGo.assignment.web.configuration;

import com.giftAndGo.assignment.adapter.interceptor.WebRequestInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication
@AllArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

    private WebRequestInterceptor webRequestInterceptor;
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(webRequestInterceptor);
    }
}
