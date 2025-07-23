package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.dto.UserReadDTO;
import com.moneymapper.budgettracker.repository.CategoryRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepo;
    private final UserService userService; // to look up the currently authenticated user

    public CategoryService(CategoryRepository categoryRepo,
            UserService userService) {
        this.categoryRepo = categoryRepo;
        this.userService = userService;
    }

    /** Return all categories (system defaults + user-created). */
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    /** Find a Category by its ID or throw if not found. */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Category findById(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
    }

    /** Create a new category under a parent (or as root if parentId == null). */
    public Category createCategory(String name, Long parentId) {
        User current = userService.getCurrentUser(); // might be null for now
        Category parent = null;
        if (parentId != null) {
            parent = findById(parentId);
        }
        Category newCat = new Category(name, parent, current);
        return categoryRepo.save(newCat);
    }

    /** Update an existing category’s name or parent. */
    public Category updateCategory(Long id, String newName, Long newParentId) {
        Category toUpdate = findById(id);

        // If ownership checks add
        // if (toUpdate.getOwner() == null ||
        // !toUpdate.getOwner().equals(userService.getCurrentUser())) {
        // throw new IllegalStateException("Cannot modify a system or someone else's
        // category");
        // }

        toUpdate.setName(newName);

        if (newParentId != null) {
            Category newParent = findById(newParentId);
            toUpdate.setParent(newParent);
        }
        return categoryRepo.save(toUpdate);
    }

    /** Delete a category (disallow if it has children). */
    public void deleteCategory(Long id) {
        Category toDelete = findById(id);

        // Optionally check ownership:
        // if (toDelete.getOwner() == null ||
        // !toDelete.getOwner().equals(userService.getCurrentUser())) {
        // throw new IllegalStateException("Cannot delete a system or someone else's
        // category");
        // }

        List<Category> children = categoryRepo.findByParent(toDelete);
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }

        categoryRepo.delete(toDelete);
    }

    public List<Category> listRootCategoriesForCurrentUser() {
        return getAllCategories();
    }
}
