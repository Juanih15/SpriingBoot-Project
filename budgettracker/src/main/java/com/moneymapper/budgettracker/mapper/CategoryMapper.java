package com.moneymapper.budgettracker.mapper;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.dto.CategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category category);
}
