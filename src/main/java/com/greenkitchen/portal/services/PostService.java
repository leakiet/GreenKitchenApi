package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.dtos.PostRequest;
import com.greenkitchen.portal.dtos.PostResponse;
import com.greenkitchen.portal.entities.Post;

public interface PostService {
    List<PostResponse> listAll();
    Post create(PostRequest req);
    Post update(Long id, PostRequest req);
    boolean existsBySlug(String slug);
    PostResponse getById(Long id);
}
