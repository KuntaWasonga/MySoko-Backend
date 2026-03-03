package com.dukani.userservice.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtPropertiesConfig {
    private String secret;
    private Long refreshTokenValidity;
    private Long accessTokenValidity;
}
