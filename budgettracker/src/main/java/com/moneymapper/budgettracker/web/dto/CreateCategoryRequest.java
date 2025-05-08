package com.moneymapper.budgettracker.web.dto;

public record CreateCategoryRequest(String name, Long parentId) {
}