package com.wit.be.infra.config;

import com.wit.be.infra.properties.AppleOAuthProperties;
import com.wit.be.infra.properties.GoogleOAuthProperties;
import com.wit.be.infra.properties.JwtProperties;
import com.wit.be.infra.properties.KakaoOAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    KakaoOAuthProperties.class,
    GoogleOAuthProperties.class,
    AppleOAuthProperties.class
})
public class PropertiesConfig {}
