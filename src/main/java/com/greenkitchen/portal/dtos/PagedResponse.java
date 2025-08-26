package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PagedResponse<T> {
    private List<T> items;
    private long total;
    private int page;
    private int size;
}
