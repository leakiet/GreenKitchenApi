package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.greenkitchen.portal.enums.PostStatus;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post extends AbstractEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false, length = 255, unique = true)
    private String slug;

    // keep simple: store author id (could be customer/employee id)
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PostCategory category;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(length = 20)
    private String priority = "normal";

    private LocalDateTime publishedAt;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.DRAFT;

}
