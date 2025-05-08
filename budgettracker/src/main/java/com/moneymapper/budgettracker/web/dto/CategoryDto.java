package com.moneymapper.budgettracker.web.dto;

import java.util.ArrayList;
import java.util.List;

public record CategoryDto(
        Long id,
        String name,
        Long parentId,
        List<CategoryDto> children) {
    public CategoryDto(Long id, String name, Long parentId) {
        this(id, name, parentId, new ArrayList<>());
    }
}
