package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;

import com.greenkitchen.portal.enums.PostStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostRequest {
    private String title;
    private String content;
    private String slug;
    private String excerpt;
    private Long authorId;
    private Long categoryId;
    private String imageUrl;
    private LocalDateTime publishedAt;
    private PostStatus status;
}
