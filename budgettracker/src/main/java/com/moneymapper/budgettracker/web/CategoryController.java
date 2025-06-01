package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.service.CategoryService;
import com.moneymapper.budgettracker.dto.CategoryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllRoots() {
        List<Category> roots = categoryService.listRootCategoriesForCurrentUser();
        List<CategoryDto> dtos = roots.stream()
                .map(cat -> new CategoryDto(cat.getId(), cat.getName(),
                        cat.getParent() == null ? null : cat.getParent().getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody CategoryDto dto) {
        Category created = categoryService.createCategory(dto.getName(), dto.getParentId());
        CategoryDto out = new CategoryDto(created.getId(), created.getName(),
                created.getParent() == null ? null : created.getParent().getId());
        return ResponseEntity.ok(out);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long id,
            @RequestBody CategoryDto dto) {
        Category updated = categoryService.updateCategory(id, dto.getName(), dto.getParentId());
        CategoryDto out = new CategoryDto(updated.getId(), updated.getName(),
                updated.getParent() == null ? null : updated.getParent().getId());
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
