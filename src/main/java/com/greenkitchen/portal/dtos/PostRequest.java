package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;


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
    private Long authorId;
    private Long categoryId;
    private String imageUrl;
    private LocalDateTime publishedAt;
    private String status;
}
