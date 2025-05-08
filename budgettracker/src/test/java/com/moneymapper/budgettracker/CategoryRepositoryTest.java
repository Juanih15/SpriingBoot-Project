package com.moneymapper.budgettracker;

import com.moneymapper.budgettracker.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    CategoryRepository categories;

    @Test
    void seededTreeHasExpectedRoots() {
        var roots = categories.findByParent(null); // top-level only
        assertThat(roots)
                .extracting("name")
                .contains("Living Expenses", "Recreation");
    }
}
