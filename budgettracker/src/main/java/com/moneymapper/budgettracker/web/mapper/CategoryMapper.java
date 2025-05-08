package com.moneymapper.budgettracker.web.mapper;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.web.dto.CategoryDto;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static CategoryDto toDto(Category c) {
        var dto = new CategoryDto(c.getId(), c.getName(),
                c.getParent() == null ? null : c.getParent().getId());
        c.getChildren().forEach(child -> dto.children().add(toDto(child)));
        return dto;
    }
}
