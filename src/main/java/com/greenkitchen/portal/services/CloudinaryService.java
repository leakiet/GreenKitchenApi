package com.greenkitchen.portal.services;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

  private final Cloudinary cloudinary;

  public Map upload(MultipartFile file) {
    try {
      Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
      return data;
    } catch (IOException io) {
      throw new RuntimeException("Image upload fail");
    }
  }

  public void delete(String imageUrl) {
    try {
      String publicId = extractPublicId(imageUrl);
      cloudinary.uploader().destroy(publicId, Map.of());
    } catch (IOException e) {
      throw new RuntimeException("Image deletion failed: " + e.getMessage());
    }
  }

  private String extractPublicId(String imageUrl) {
    int uploadIndex = imageUrl.indexOf("/upload/");
    if (uploadIndex == -1)
      throw new RuntimeException("Invalid Cloudinary URL");
    String afterUpload = imageUrl.substring(uploadIndex + "/upload/".length());
    
    if (afterUpload.matches("v\\d+/.*")) {
      afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
    }
    int dotIndex = afterUpload.lastIndexOf('.');
    return dotIndex == -1 ? afterUpload : afterUpload.substring(0, dotIndex);
  }
}
