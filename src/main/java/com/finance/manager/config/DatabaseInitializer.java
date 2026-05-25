package com.finance.manager.config;

import com.finance.manager.entity.Category;
import com.finance.manager.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DatabaseInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedCategory("Salary", "INCOME");
        seedCategory("Food", "EXPENSE");
        seedCategory("Rent", "EXPENSE");
        seedCategory("Transportation", "EXPENSE");
        seedCategory("Entertainment", "EXPENSE");
        seedCategory("Healthcare", "EXPENSE");
        seedCategory("Utilities", "EXPENSE");
    }

    private void seedCategory(String name, String type) {
        if (!categoryRepository.existsByNameAndUserIsNull(name)) {
            Category category = new Category(name, type, false, null);
            categoryRepository.save(category);
        }
    }
}
