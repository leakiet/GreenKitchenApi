package com.greenkitchen.portal.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.greenkitchen.portal.dtos.PagedResponse;
import com.greenkitchen.portal.dtos.PostRequest;
import com.greenkitchen.portal.dtos.PostResponse;
import com.greenkitchen.portal.entities.Post;

public interface PostService {
    List<PostResponse> listAll();
    PagedResponse<PostResponse> listPaged(int page, int size);
    PagedResponse<PostResponse> listFilteredPaged(int page, int size, String status, Long categoryId, String q);
    Post create(PostRequest req);
    Post update(Long id, PostRequest req);
    boolean existsBySlug(String slug);
    PostResponse getById(Long id);
    PostResponse getBySlug(String slug);
    String uploadImage(MultipartFile file);
}
