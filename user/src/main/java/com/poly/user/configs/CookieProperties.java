package com.poly.user.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt.cookie")
public class CookieProperties {
  private String accessName = "access_token";
  private String refreshName = "refresh_token";
  private boolean secure = true;
  private String sameSite = "Strict";
}
