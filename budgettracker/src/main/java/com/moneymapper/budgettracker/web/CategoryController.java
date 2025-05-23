package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.dto.CategoryDto;
import com.moneymapper.budgettracker.dto.CreateCategoryRequest;
import com.moneymapper.budgettracker.mapper.CategoryMapper;
import com.moneymapper.budgettracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categories;
    private final CategoryMapper mapper;

    @GetMapping
    public List<CategoryDto> all(@AuthenticationPrincipal User user) {
        return categories.findByOwnerIsNullOrOwner(user).stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody CreateCategoryRequest req,
            @AuthenticationPrincipal User user) {

        Category parent = req.parentId() == null
                ? null
                : categories.findById(req.parentId())
                        .orElseThrow(() -> new IllegalArgumentException("parent not found"));

        var saved = categories.save(new Category(req.name(), parent, user));
        return mapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public CategoryDto rename(@PathVariable Long id,
            @RequestBody String newName,
            @AuthenticationPrincipal User user) {

        var original = categories.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found"));

        // optional: owner-check => if (!original.getOwner().equals(user)) throw …

        var replacement = new Category(newName, original.getParent(), original.getOwner());
        categories.delete(original);
        var saved = categories.save(replacement);
        return mapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categories.deleteById(id);
    }
}
