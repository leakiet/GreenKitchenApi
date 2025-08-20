package com.greenkitchen.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.entities.PostCategory;
import com.greenkitchen.portal.repositories.PostCategoryRepository;

@RestController
@RequestMapping("/apis/v1/post-categories")
public class PostCategoryController {

    @Autowired
    private PostCategoryRepository postCategoryRepository;

    @GetMapping
    public ResponseEntity<List<PostCategory>> list() {
        List<PostCategory> cats = postCategoryRepository.findAll();
        return ResponseEntity.ok(cats);
    }
}
