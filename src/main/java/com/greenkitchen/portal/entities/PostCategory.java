package com.greenkitchen.portal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_categories")
@Getter
@Setter
@NoArgsConstructor
public class PostCategory extends AbstractEntity {
    @Column(nullable = false, length = 255, unique = true)
    private String name;

    @Column(length = 255, unique = true)
    private String slug;
}
