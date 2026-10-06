package com.poly.minio.controllers;

import com.poly.minio.dtos.responses.ImageResponse;
import com.poly.minio.services.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

  private final MinioService minioService;

  @PostMapping
  public ResponseEntity<ImageResponse> uploadImage(
      @RequestParam("file") MultipartFile file, @RequestParam("folder") String folder) {

    String objectKey = minioService.uploadImage(file, folder);
    String url = minioService.getPresignedUrl(objectKey);

    return ResponseEntity.ok(new ImageResponse(objectKey, url));
  }

  @GetMapping("/url")
  public ResponseEntity<ImageResponse> getImageUrl(@RequestParam("objectKey") String objectKey) {
    String url = minioService.getPresignedUrl(objectKey);
    return ResponseEntity.ok(new ImageResponse(objectKey, url));
  }
}
