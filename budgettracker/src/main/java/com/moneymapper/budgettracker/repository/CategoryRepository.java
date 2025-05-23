package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOwnerIsNullOrOwner(User owner);

    List<Category> findByParent(Category parent);
}
