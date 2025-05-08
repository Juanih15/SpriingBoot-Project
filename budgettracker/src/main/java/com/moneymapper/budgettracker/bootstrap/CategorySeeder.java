package com.moneymapper.budgettracker.bootstrap;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categories;

    public CategorySeeder(CategoryRepository categories) {
        this.categories = categories;
    }

    @Override
    public void run(String... args) {
        if (categories.count() > 0)
            return; // already seeded

        /* Top-level nodes */
        var living = categories.save(new Category("Living Expenses", null, null));
        var rec = categories.save(new Category("Recreation", null, null));

        /* Living-Expenses children */
        var utils = categories.save(new Category("Utilities", living, null));
        categories.saveAll(List.of(
                new Category("Rent / Mortgage", living, null),
                new Category("Groceries", living, null),

                /* Utilities → grandchildren */
                new Category("Water", utils, null),
                new Category("Electricity", utils, null),
                new Category("Internet", utils, null)));

        /* Recreation children */
        categories.saveAll(List.of(
                new Category("Gym Membership", rec, null),
                new Category("Karate Classes", rec, null)));
    }
}
