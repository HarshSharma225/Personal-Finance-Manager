package com.finance.manager.service;

import com.finance.manager.dto.CategoryDto;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.exception.CustomException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository, UserService userService, @Lazy TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public CategoryDto.CategoryListResponse getAllCategories() {
        User currentUser = userService.getCurrentUser();
        List<Category> systemCategories = categoryRepository.findByUserIsNull();
        List<Category> userCategories = categoryRepository.findByUser(currentUser);

        List<CategoryDto.CategoryResponse> responses = new ArrayList<>();
        
        for (Category cat : systemCategories) {
            responses.add(new CategoryDto.CategoryResponse(cat.getName(), cat.getType(), cat.isCustom()));
        }
        for (Category cat : userCategories) {
            responses.add(new CategoryDto.CategoryResponse(cat.getName(), cat.getType(), cat.isCustom()));
        }

        return new CategoryDto.CategoryListResponse(responses);
    }

    @Transactional
    public CategoryDto.CategoryResponse createCategory(CategoryDto.CreateCategoryRequest request) {
        User currentUser = userService.getCurrentUser();
        
        String name = request.getName();
        String type = request.getType();

        if (categoryRepository.existsByNameAndUserIsNull(name)) {
            throw new CustomException.ConflictException("Predefined category with this name already exists");
        }

        if (categoryRepository.existsByNameAndUser(name, currentUser)) {
            throw new CustomException.ConflictException("Custom category with this name already exists");
        }

        Category category = new Category(name, type, true, currentUser);
        Category saved = categoryRepository.save(category);
        return new CategoryDto.CategoryResponse(saved.getName(), saved.getType(), saved.isCustom());
    }

    @Transactional
    public void deleteCategory(String name) {
        User currentUser = userService.getCurrentUser();

        Optional<Category> defaultCategoryOpt = categoryRepository.findByNameAndUserIsNull(name);
        if (defaultCategoryOpt.isPresent()) {
            throw new CustomException.BadRequestException("Predefined categories cannot be deleted");
        }

        Category category = categoryRepository.findByNameAndUser(name, currentUser)
                .orElseThrow(() -> new CustomException.ResourceNotFoundException("Category not found"));

        if (transactionRepository.existsByCategory(category)) {
            throw new CustomException.BadRequestException("Category is in use by transactions and cannot be deleted");
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public Category getValidCategory(String name, User user) {
        return categoryRepository.findByNameAndUser(name, user)
                .orElseGet(() -> categoryRepository.findByNameAndUserIsNull(name)
                .orElseThrow(() -> new CustomException.BadRequestException("Invalid category name: " + name)));
    }
}
