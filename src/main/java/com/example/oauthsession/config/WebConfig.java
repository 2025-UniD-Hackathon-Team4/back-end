package com.example.oauthsession.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value( "${upstage.api.url}")
    private String upstageApiUrl;

    @Value( "${upstage.api.key}")
    private String upstageApiKey;

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }

    @Bean
    public WebClient upstageWebClient(){
        return WebClient.builder()
                .baseUrl(upstageApiUrl)
                .defaultHeader("Authorization", "Bearer " + upstageApiKey)
                .defaultHeader("Content-Type","application/json")
                .build();
    }
}
