package com.greenkitchen.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.greenkitchen.portal.dtos.PostRequest;
import com.greenkitchen.portal.dtos.PostResponse;
import com.greenkitchen.portal.services.PostService;

@RestController
@RequestMapping("/apis/v1/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "size", required = false) Integer size) {
        if (page != null && size != null) {
            var resp = postService.listPaged(page, size);
            return ResponseEntity.ok(resp);
        }
        List<PostResponse> list = postService.listAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> listFiltered(@RequestParam(value = "page", required = false) Integer page,
                                          @RequestParam(value = "size", required = false) Integer size,
                                          @RequestParam(value = "status", required = false) String status,
                                          @RequestParam(value = "categoryId", required = false) Long categoryId,
                                          @RequestParam(value = "q", required = false) String q) {
        if (page != null && size != null) {
            var resp = postService.listFilteredPaged(page, size, status, categoryId, q);
            return ResponseEntity.ok(resp);
        }
        List<PostResponse> list = postService.listAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable("id") Long id) {
        PostResponse resp = postService.getById(id);
        if (resp == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponse> getBySlug(@PathVariable("slug") String slug) {
        PostResponse resp = postService.getBySlug(slug);
        if (resp == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody PostRequest req) {
        try {
            var p = postService.create(req);
            // return DTO to the client to avoid serializing JPA entity graphs
            PostResponse resp = postService.getById(p.getId());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(@PathVariable("id") Long id, @RequestBody PostRequest req) {
        var updated = postService.update(id, req);
        if (updated == null)
            return ResponseEntity.notFound().build();
        PostResponse resp = postService.getById(updated.getId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("imageFile") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String imageUrl = postService.uploadImage(file);
        return ResponseEntity.ok(imageUrl);
    }

}
