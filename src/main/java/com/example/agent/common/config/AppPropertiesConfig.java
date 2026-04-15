package com.example.agent.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppSecurityProperties.class)
public class AppPropertiesConfig {
}
