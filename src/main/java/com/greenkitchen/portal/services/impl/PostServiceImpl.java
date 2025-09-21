package com.greenkitchen.portal.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import com.greenkitchen.portal.dtos.PostRequest;
import com.greenkitchen.portal.dtos.PostResponse;
import com.greenkitchen.portal.dtos.PagedResponse;
import com.greenkitchen.portal.entities.Post;
import com.greenkitchen.portal.entities.PostCategory;
import com.greenkitchen.portal.enums.PostStatus;
import com.greenkitchen.portal.repositories.PostCategoryRepository;
import com.greenkitchen.portal.repositories.PostRepository;
import com.greenkitchen.portal.services.PostService;
import com.greenkitchen.portal.utils.ImageUtils;
import com.greenkitchen.portal.utils.SlugUtils;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCategoryRepository postCategoryRepository;

    @Autowired
    private ImageUtils imageUtils;

    @Override
    public List<PostResponse> listAll() {
        return postRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList()) ;
    }

    @Override
    public PagedResponse<PostResponse> listPaged(int page, int size) {
        Pageable pg = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
        Page<Post> p = postRepository.findAllActivePaged(pg);
        PagedResponse<PostResponse> resp = new PagedResponse<>();
        resp.setItems(p.stream().map(this::toResponse).collect(Collectors.toList()));
        resp.setTotal(p.getTotalElements());
        resp.setPage(page);
        resp.setSize(size);
        return resp;
    }

    @Override
    public PagedResponse<PostResponse> listFilteredPaged(int page, int size, String status, Long categoryId, String q) {
        Pageable pg = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
        PostStatus st = null;
        if (status != null && !status.isEmpty()) {
            try {
                st = PostStatus.valueOf(status);
            } catch (IllegalArgumentException ex) {
                st = null;
            }
        }
        Page<Post> p = postRepository.findFilteredPaged(st, categoryId, q == null || q.isEmpty() ? null : q, pg);
        PagedResponse<PostResponse> resp = new PagedResponse<>();
        resp.setItems(p.stream().map(this::toResponse).collect(Collectors.toList()));
        resp.setTotal(p.getTotalElements());
        resp.setPage(page);
        resp.setSize(size);
        return resp;
    }

    @Override
    public Post create(PostRequest req) {
        Post p = new Post();
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());

        String baseSlug = SlugUtils.toSlug(req.getSlug() != null && !req.getSlug().isEmpty() ? req.getSlug() : req.getTitle());
        String uniqueSlug = SlugUtils.generateUniqueSlug(baseSlug, slug -> postRepository.existsBySlug(slug));
        p.setSlug(uniqueSlug);

        p.setAuthorId(req.getAuthorId());
        if (req.getCategoryId() != null) {
            PostCategory cat = postCategoryRepository.findById(req.getCategoryId()).orElse(null);
            p.setCategory(cat);
        }
        p.setImageUrl(req.getImageUrl());
        p.setPriority(req.getPriority() != null ? req.getPriority() : "normal");
        p.setPublishedAt(req.getPublishedAt());
        // parse status string from request, default to DRAFT
        if (req.getStatus() == null) {
            p.setStatus(PostStatus.DRAFT);
        } else {
            try {
                p.setStatus(PostStatus.valueOf(req.getStatus()));
            } catch (IllegalArgumentException ex) {
                p.setStatus(PostStatus.DRAFT);
            }
        }

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

        if (req.getCategoryId() != null) {
            PostCategory cat = postCategoryRepository.findById(req.getCategoryId()).orElse(null);
            existing.setCategory(cat);
        }
        existing.setImageUrl(req.getImageUrl() != null ? req.getImageUrl() : existing.getImageUrl());
        existing.setPriority(req.getPriority() != null ? req.getPriority() : existing.getPriority());
        existing.setPublishedAt(req.getPublishedAt() != null ? req.getPublishedAt() : existing.getPublishedAt());
        // update status from string in request if provided
        if (req.getStatus() != null) {
            try {
                existing.setStatus(PostStatus.valueOf(req.getStatus()));
            } catch (IllegalArgumentException ex) {
                // ignore invalid status and keep existing
            }
        }

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

    @Override
    public PostResponse getBySlug(String slug) {
        if (slug == null || slug.isEmpty()) return null;
        Post p = postRepository.findBySlug(slug);
        return p == null ? null : toResponse(p);
    }

    private PostResponse toResponse(Post p) {
//         PostResponse r = new PostResponse();
//         r.setId(p.getId());
//         r.setTitle(p.getTitle());
//         r.setContent(p.getContent());
//         r.setSlug(p.getSlug());
//         r.setAuthorId(p.getAuthorId());
        ModelMapper modelMapper = new ModelMapper();
        PostResponse r = modelMapper.map(p, PostResponse.class);
        // Manually set categoryId and categoryName if category exists
        if (p.getCategory() != null) {
            r.setCategoryId(p.getCategory().getId());
            r.setCategoryName(p.getCategory().getName());
        }
//         r.setImageUrl(p.getImageUrl());
//         r.setPublishedAt(p.getPublishedAt());
//     // send status as string for easier frontend handling
//     r.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
//         r.setCreatedAt(p.getCreatedAt());
//         r.setUpdatedAt(p.getUpdatedAt());
        // Send status as string for easier frontend handling
        r.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        return r;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Invalid file");
        }

        return imageUtils.uploadImage(file);
    }
}
