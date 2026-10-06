package com.poly.minio.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
  private String endpoint;
  private String publicEndpoint;
  private String accessKey;
  private String secretKey;
  private String bucketName;
  private int linkExpiry = 3600;
}
