package com.moneymapper.budgettracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.domain.User;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Find all “root” categories (parent == null) for a given user, plus system
    // defaults (owner == null):
    List<Category> findByOwnerOrOwnerIsNullAndParentIsNull(User owner);

    // Find children of a given category (so you can display nested lists):
    List<Category> findByParent(Category parent);

    // Optionally, find by name for a user (to prevent duplicates under the same
    // parent+user):
    boolean existsByNameAndOwnerAndParent(String name, User owner, Category parent);
}
