package io.verticle.k8s.oculus;

import io.verticle.oss.fireboard.client.FireboardAccessConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jens Saade
 */
@Configuration
public class FireboardAccessConfigMap {

    @ConfigurationProperties
    @Bean
    public FireboardAccessConfig configure() {
        FireboardAccessConfig config = new FireboardAccessConfig();
        return config;
    }
}