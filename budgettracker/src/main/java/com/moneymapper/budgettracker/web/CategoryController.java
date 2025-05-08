package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.repository.CategoryRepository;
import com.moneymapper.budgettracker.web.dto.CategoryDto;
import com.moneymapper.budgettracker.web.dto.CreateCategoryRequest;
import com.moneymapper.budgettracker.web.mapper.CategoryMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categories;

    public CategoryController(CategoryRepository categories) {
        this.categories = categories;
    }

    /** entire tree (system defaults + user’s custom nodes) */
    @GetMapping
    public List<CategoryDto> all(@AuthenticationPrincipal User user) {
        var roots = categories.findByParent(null); // top-level nodes
        return roots.stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    /** add a custom sub-category (owner = current user) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody CreateCategoryRequest req,
            @AuthenticationPrincipal User user) {

        Category parent = req.parentId() == null
                ? null
                : categories.findById(req.parentId())
                        .orElseThrow(() -> new IllegalArgumentException("parent not found"));

        var saved = categories.save(new Category(req.name(), parent, user));
        return CategoryMapper.toDto(saved);
    }

    /** rename a category (owner check omitted for brevity) */
    @PatchMapping("/{id}")
    public CategoryDto rename(@PathVariable Long id, @RequestBody String newName) {
        var cat = categories.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found"));
        cat.setName(newName);
        return CategoryMapper.toDto(categories.save(cat));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categories.deleteById(id);
    }
}
