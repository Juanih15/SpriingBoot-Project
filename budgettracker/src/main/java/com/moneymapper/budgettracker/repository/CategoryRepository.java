package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // system defaults the whole app shares
    List<Category> findByOwnerIsNullOrderByName();

    // categories a single user created
    List<Category> findByOwnerOrderByName(User owner);

    // navigate tree
    List<Category> findByParent(Category parent);
}
