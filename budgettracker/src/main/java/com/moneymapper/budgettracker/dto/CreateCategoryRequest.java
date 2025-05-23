package com.moneymapper.budgettracker.dto;

public record CreateCategoryRequest(String name, Long parentId) {
}