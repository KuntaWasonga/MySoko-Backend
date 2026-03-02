package com.dukani.apigateway.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtPropertiesConfig {
    private String secret;
}
