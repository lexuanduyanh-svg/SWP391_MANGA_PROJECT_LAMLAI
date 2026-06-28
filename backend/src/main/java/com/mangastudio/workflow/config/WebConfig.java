package com.mangastudio.workflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final CorsProperties corsProperties;

  public WebConfig(CorsProperties corsProperties) {
    this.corsProperties = corsProperties;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] origins = corsProperties.getAllowedOrigins().toArray(new String[0]);
    String[] methods = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"};

    registry
        .addMapping("/api/**")
        .allowedOrigins(origins)
        .allowedMethods(methods)
        .allowedHeaders("*");

    registry
        .addMapping("/admin/**")
        .allowedOrigins(origins)
        .allowedMethods(methods)
        .allowedHeaders("*");
  }
}
