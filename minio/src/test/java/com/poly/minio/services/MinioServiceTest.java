package com.poly.minio.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poly.minio.configs.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

  @Mock private MinioClient minioClient;

  @Mock private MinioProperties minioProperties;

  @InjectMocks private MinioService minioService;

  @BeforeEach
  void setUp() {
    // Only stub getBucketName if it will be used, mostly everything uses it.
  }

  private MockMultipartFile createValidImageFile() {
    return new MockMultipartFile("file", "test.png", "image/png", "test image content".getBytes());
  }

  @Nested
  class UploadAndLink {

    @Test
    void uploadImage_whenValid_returnsObjectKey() throws Exception {
      when(minioProperties.getBucketName()).thenReturn("test-bucket");
      MockMultipartFile file = createValidImageFile();
      String folder = "hotels/1_test";

      String result = minioService.uploadImage(file, folder);

      assertThat(result).startsWith("hotels/1_test/");
      assertThat(result).endsWith("-test.png");

      ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
      verify(minioClient, times(1)).putObject(captor.capture());

      PutObjectArgs args = captor.getValue();
      assertThat(args.bucket()).isEqualTo("test-bucket");
      assertThat(args.object()).isEqualTo(result);
      assertThat(args.contentType()).isEqualTo("image/png");
    }

    @Test
    void getPresignedUrl_whenValid_returnsUrl() throws Exception {
      when(minioProperties.getBucketName()).thenReturn("test-bucket");
      when(minioProperties.getLinkExpiry()).thenReturn(3600);
      when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
          .thenReturn("http://localhost:9000/test-bucket/test.png?token=abc");

      String result = minioService.getPresignedUrl("hotels/1/test.png");

      assertThat(result).isEqualTo("http://localhost:9000/test-bucket/test.png?token=abc");
      verify(minioClient, times(1)).statObject(any(StatObjectArgs.class));
    }
  }

  @Nested
  class BucketStartupCheck {

    @Test
    void init_whenBucketExists_doesNothing() throws Exception {
      when(minioProperties.getBucketName()).thenReturn("test-bucket");
      when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

      minioService.init();

      // Should not throw exception
    }

    @Test
    void init_whenBucketDoesNotExist_throwsException() throws Exception {
      when(minioProperties.getBucketName()).thenReturn("test-bucket");
      when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

      assertThatThrownBy(() -> minioService.init())
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Failed to connect");
    }

    @Test
    void init_whenClientThrowsException_throwsException() throws Exception {
      when(minioProperties.getBucketName()).thenReturn("test-bucket");
      when(minioClient.bucketExists(any(BucketExistsArgs.class)))
          .thenThrow(new RuntimeException("Connection refused"));

      assertThatThrownBy(() -> minioService.init())
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Failed to connect");
    }
  }

  @Nested
  class Validation {

    @Test
    void uploadImage_whenInvalidContentType_throwsException() throws Exception {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

      assertThatThrownBy(() -> minioService.uploadImage(file, "folder"))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Only image/jpeg, image/png, and image/webp are allowed")
          .extracting("statusCode")
          .isEqualTo(HttpStatus.BAD_REQUEST);

      verify(minioClient, never()).putObject(any());
    }

    @Test
    void uploadImage_whenInvalidPath_throwsException() throws Exception {
      MockMultipartFile file = createValidImageFile();

      assertThatThrownBy(() -> minioService.uploadImage(file, "../hotels"))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Invalid characters in path");

      assertThatThrownBy(() -> minioService.uploadImage(file, "/hotels"))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Path cannot start with a slash");

      assertThatThrownBy(() -> minioService.uploadImage(file, "hotels//test"))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Invalid characters in path");

      verify(minioClient, never()).putObject(any());
    }

    @Test
    void getPresignedUrl_withPublicEndpointSwap() throws Exception {
      when(minioProperties.getBucketName()).thenReturn("test-bucket");
      when(minioProperties.getLinkExpiry()).thenReturn(3600);
      when(minioProperties.getPublicEndpoint()).thenReturn("https://cdn.example.com");
      when(minioProperties.getEndpoint()).thenReturn("http://localhost:9000");

      when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
          .thenReturn("http://localhost:9000/test-bucket/test.png?token=abc");

      String result = minioService.getPresignedUrl("hotels/1/test.png");

      assertThat(result).isEqualTo("https://cdn.example.com/test-bucket/test.png?token=abc");
    }
  }

  @Nested
  class ErrorsAndEdgeCases {

    @Test
    void getPresignedUrl_whenObjectNotFound_throwsNotFoundException() throws Exception {
      when(minioProperties.getBucketName()).thenReturn("test-bucket");

      ErrorResponseException minioEx = mock(ErrorResponseException.class);
      io.minio.messages.ErrorResponse errRes = mock(io.minio.messages.ErrorResponse.class);
      when(minioEx.errorResponse()).thenReturn(errRes);
      when(errRes.code()).thenReturn("NoSuchKey");

      when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(minioEx);

      assertThatThrownBy(() -> minioService.getPresignedUrl("missing/file.png"))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Image not found")
          .extracting("statusCode")
          .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void uploadImage_whenMinioThrowsException_throwsInternalServerError() throws Exception {
      when(minioProperties.getBucketName()).thenReturn("test-bucket");
      MockMultipartFile file = createValidImageFile();
      String folder = "hotels";

      when(minioClient.putObject(any(PutObjectArgs.class)))
          .thenThrow(new RuntimeException("MinIO error"));

      assertThatThrownBy(() -> minioService.uploadImage(file, folder))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Failed to upload file")
          .extracting("statusCode")
          .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
