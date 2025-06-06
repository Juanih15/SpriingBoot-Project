package com.moneymapper.budgettracker.dto;

// A DTO that includes id, name, and parentId (if any).
public class CategoryDto {
    private Long id;
    private String name;
    private Long parentId;

    public CategoryDto() {
    }

    public CategoryDto(Long id, String name, Long parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
