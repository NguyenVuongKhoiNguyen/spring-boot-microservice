package com.poly.minio.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import java.io.ByteArrayInputStream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ImageControllerTest {

  @Container
  static MinIOContainer minioContainer =
      new MinIOContainer("minio/minio").withUserName("admin").withPassword("password");

  static MinioClient minioClient;
  static final String BUCKET = "test-bucket";

  @BeforeAll
  static void setUpMinio() throws Exception {
    minioClient =
        MinioClient.builder()
            .endpoint(minioContainer.getS3URL())
            .credentials(minioContainer.getUserName(), minioContainer.getPassword())
            .build();

    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
    }
  }

  @DynamicPropertySource
  static void minioProperties(DynamicPropertyRegistry registry) {
    registry.add("minio.endpoint", minioContainer::getS3URL);
    registry.add("minio.public-endpoint", minioContainer::getS3URL);
    registry.add("minio.access-key", minioContainer::getUserName);
    registry.add("minio.secret-key", minioContainer::getPassword);
    registry.add("minio.bucket-name", () -> BUCKET);
  }

  @Autowired private MockMvc mockMvc;

  @Test
  void uploadImage_whenValidPng_returns200AndExistsInMinio() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.png", "image/png", "dummy-image-content".getBytes());

    MvcResult result =
        mockMvc
            .perform(multipart("/api/images").file(file).param("folder", "hotels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.objectKey").value(Matchers.startsWith("hotels/")))
            .andExpect(jsonPath("$.url").isNotEmpty())
            .andReturn();

    // Check if object exists in MinIO
    String responseBody = result.getResponse().getContentAsString();
    String objectKey = JsonPath.read(responseBody, "$.objectKey");

    minioClient.statObject(StatObjectArgs.builder().bucket(BUCKET).object(objectKey).build());
  }

  @Test
  void uploadImage_whenNestedFolder_returns200() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "nested.png", "image/png", "content".getBytes());

    mockMvc
        .perform(multipart("/api/images").file(file).param("folder", "hotels/test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.objectKey").value(Matchers.startsWith("hotels/test/")));
  }

  @Test
  void getImageUrl_whenUploadedKey_returnsPresignedUrl() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "link.png", "image/png", "content".getBytes());

    MvcResult uploadResult =
        mockMvc
            .perform(multipart("/api/images").file(file).param("folder", "hotels"))
            .andExpect(status().isOk())
            .andReturn();

    String objectKey =
        JsonPath.read(uploadResult.getResponse().getContentAsString(), "$.objectKey");

    mockMvc
        .perform(get("/api/images/url").param("objectKey", objectKey))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.objectKey").value(objectKey))
        .andExpect(jsonPath("$.url").value(Matchers.containsString("X-Amz-Signature=")));
  }

  @Test
  void getImageUrl_whenTestPutsKeyDirectly_returnsLink() throws Exception {
    String key = "hotels/9_Hostel_and_Bar/0.jpg";
    minioClient.putObject(
        PutObjectArgs.builder().bucket(BUCKET).object(key).stream(
                new ByteArrayInputStream("direct".getBytes()), 6, -1)
            .contentType("image/jpeg")
            .build());

    mockMvc
        .perform(get("/api/images/url").param("objectKey", key))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.objectKey").value(key))
        .andExpect(jsonPath("$.url").value(Matchers.containsString("X-Amz-Signature=")));
  }

  @Test
  void getImageUrl_whenMissingKey_returns404() throws Exception {
    mockMvc
        .perform(get("/api/images/url").param("objectKey", "missing/file.png"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("404 NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("Image not found"));
  }

  @Test
  void uploadImage_whenPdfFile_returns400() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());

    mockMvc
        .perform(multipart("/api/images").file(file).param("folder", "hotels"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("400 BAD_REQUEST"))
        .andExpect(
            jsonPath("$.message").value("Only image/jpeg, image/png, and image/webp are allowed"));
  }

  @Test
  void uploadImage_whenOver10MB_returns400() throws Exception {
    // Create an 11MB file
    byte[] largeContent = new byte[11 * 1024 * 1024];
    MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", largeContent);

    mockMvc
        .perform(multipart("/api/images").file(file).param("folder", "hotels"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("File too large"));
  }

  @Test
  void uploadImage_whenFolderContainsDotDot_returns400() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());

    mockMvc
        .perform(multipart("/api/images").file(file).param("folder", "../hotels"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("400 BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Invalid characters in path"));
  }

  @Test
  void uploadImage_whenEmptyFile_returns400() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

    mockMvc
        .perform(multipart("/api/images").file(file).param("folder", "hotels"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("400 BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("File cannot be empty"));
  }

  @Test
  void uploadImage_whenMissingFile_returns400() throws Exception {
    mockMvc
        .perform(multipart("/api/images").param("folder", "hotels"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }
}
