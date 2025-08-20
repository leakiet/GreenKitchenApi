package com.greenkitchen.portal.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.PostRequest;
import com.greenkitchen.portal.dtos.PostResponse;
import com.greenkitchen.portal.entities.Post;
import com.greenkitchen.portal.entities.PostCategory;
import com.greenkitchen.portal.repositories.PostCategoryRepository;
import com.greenkitchen.portal.repositories.PostRepository;
import com.greenkitchen.portal.services.PostService;
import com.greenkitchen.portal.utils.SlugUtils;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCategoryRepository postCategoryRepository;

    @Override
    public List<PostResponse> listAll() {
        List<Post> posts = postRepository.findAllActive();
        return posts.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Post create(PostRequest req) {
        Post p = new Post();
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());

        String baseSlug = SlugUtils.toSlug(req.getSlug() != null && !req.getSlug().isEmpty() ? req.getSlug() : req.getTitle());
        String uniqueSlug = SlugUtils.generateUniqueSlug(baseSlug, slug -> postRepository.existsBySlug(slug));
        p.setSlug(uniqueSlug);

        p.setExcerpt(req.getExcerpt());
        p.setAuthorId(req.getAuthorId());
        if (req.getCategoryId() != null) {
            PostCategory cat = postCategoryRepository.findById(req.getCategoryId()).orElse(null);
            p.setCategory(cat);
        }
        p.setImageUrl(req.getImageUrl());
        p.setPublishedAt(req.getPublishedAt());
        p.setStatus(req.getStatus() == null ? com.greenkitchen.portal.enums.PostStatus.DRAFT : req.getStatus());

        return postRepository.save(p);
    }

    @Override
    public Post update(Long id, PostRequest req) {
        Post existing = postRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setTitle(req.getTitle() != null ? req.getTitle() : existing.getTitle());
        existing.setContent(req.getContent() != null ? req.getContent() : existing.getContent());

        if (req.getSlug() != null && !req.getSlug().isEmpty() && !req.getSlug().equals(existing.getSlug())) {
            String baseSlug = SlugUtils.toSlug(req.getSlug());
            String uniqueSlug = SlugUtils.generateUniqueSlug(baseSlug, slug -> postRepository.existsBySlug(slug) && !slug.equals(existing.getSlug()));
            existing.setSlug(uniqueSlug);
        }

        existing.setExcerpt(req.getExcerpt() != null ? req.getExcerpt() : existing.getExcerpt());
        if (req.getCategoryId() != null) {
            PostCategory cat = postCategoryRepository.findById(req.getCategoryId()).orElse(null);
            existing.setCategory(cat);
        }
        existing.setImageUrl(req.getImageUrl() != null ? req.getImageUrl() : existing.getImageUrl());
        existing.setPublishedAt(req.getPublishedAt() != null ? req.getPublishedAt() : existing.getPublishedAt());
        existing.setStatus(req.getStatus() != null ? req.getStatus() : existing.getStatus());

        existing.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(existing);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return postRepository.existsBySlug(slug);
    }

    @Override
    public PostResponse getById(Long id) {
        Post p = postRepository.findById(id).orElse(null);
        return p == null ? null : toResponse(p);
    }

    private PostResponse toResponse(Post p) {
        PostResponse r = new PostResponse();
        r.setId(p.getId());
        r.setTitle(p.getTitle());
        r.setContent(p.getContent());
        r.setSlug(p.getSlug());
        r.setExcerpt(p.getExcerpt());
        r.setAuthorId(p.getAuthorId());
        if (p.getCategory() != null) {
            r.setCategoryId(p.getCategory().getId());
            r.setCategoryName(p.getCategory().getName());
        }
        r.setImageUrl(p.getImageUrl());
        r.setPublishedAt(p.getPublishedAt());
        r.setStatus(p.getStatus());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
