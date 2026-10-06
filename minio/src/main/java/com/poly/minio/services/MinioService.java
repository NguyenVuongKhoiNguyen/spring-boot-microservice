package com.poly.minio.services;

import com.poly.minio.configs.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

  private final MinioClient minioClient;
  private final MinioProperties minioProperties;
  private static final Pattern VALID_NAME_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_\\-\\.]+(/[a-zA-Z0-9_\\-\\.]+)*$");

  @PostConstruct
  public void init() {
    try {
      boolean found =
          minioClient.bucketExists(
              BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
      if (!found) {
        log.error(
            "MinIO bucket '{}' does not exist. Please create it manually.",
            minioProperties.getBucketName());
        throw new IllegalStateException(
            "MinIO bucket '" + minioProperties.getBucketName() + "' does not exist.");
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to connect to MinIO or check bucket existence", e);
    }
  }

  public String uploadImage(MultipartFile file, String folder) {
    validateContentType(file.getContentType());
    validatePath(folder);

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      originalFilename = "unnamed";
    }
    validatePath(originalFilename);

    String objectKey = folder + "/" + UUID.randomUUID() + "-" + originalFilename;

    try (InputStream inputStream = file.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(minioProperties.getBucketName()).object(objectKey).stream(
                  inputStream, file.getSize(), -1)
              .contentType(file.getContentType())
              .build());

      return objectKey;
    } catch (Exception e) {
      log.error("Failed to upload file to MinIO", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
    }
  }

  public String getPresignedUrl(String objectKey) {
    validatePath(objectKey);

    try {
      minioClient.statObject(
          StatObjectArgs.builder()
              .bucket(minioProperties.getBucketName())
              .object(objectKey)
              .build());

      String url =
          minioClient.getPresignedObjectUrl(
              GetPresignedObjectUrlArgs.builder()
                  .method(Method.GET)
                  .bucket(minioProperties.getBucketName())
                  .object(objectKey)
                  .expiry(minioProperties.getLinkExpiry(), TimeUnit.SECONDS)
                  .build());

      if (minioProperties.getPublicEndpoint() != null
          && !minioProperties.getPublicEndpoint().isEmpty()) {
        String internalEndpoint = minioProperties.getEndpoint();
        if (url.startsWith(internalEndpoint)) {
          url = url.replaceFirst(internalEndpoint, minioProperties.getPublicEndpoint());
        }
      }
      return url;
    } catch (io.minio.errors.ErrorResponseException e) {
      if ("NoSuchKey".equals(e.errorResponse().code())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
      }
      log.error("Failed to generate presigned URL", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate URL");
    } catch (Exception e) {
      log.error("Failed to generate presigned URL", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate URL");
    }
  }

  private void validateContentType(String contentType) {
    if (contentType == null
        || !(contentType.equals("image/jpeg")
            || contentType.equals("image/png")
            || contentType.equals("image/webp"))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Only image/jpeg, image/png, and image/webp are allowed");
    }
  }

  private void validatePath(String path) {
    if (path == null || path.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path or filename cannot be empty");
    }
    if (path.startsWith("/")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path cannot start with a slash");
    }
    if (path.contains("..") || path.contains("\\") || path.contains("//")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid characters in path");
    }
    if (!VALID_NAME_PATTERN.matcher(path).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path contains invalid characters");
    }
  }
}
