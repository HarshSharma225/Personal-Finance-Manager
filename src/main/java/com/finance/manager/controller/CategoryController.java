package com.finance.manager.controller;

import com.finance.manager.dto.CategoryDto;
import com.finance.manager.dto.UserDto;
import com.finance.manager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<CategoryDto.CategoryListResponse> getAllCategories() {
        CategoryDto.CategoryListResponse response = categoryService.getAllCategories();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CategoryDto.CategoryResponse> createCategory(@Valid @RequestBody CategoryDto.CreateCategoryRequest request) {
        CategoryDto.CategoryResponse response = categoryService.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<UserDto.MessageResponse> deleteCategory(@PathVariable String name) {
        categoryService.deleteCategory(name);
        return new ResponseEntity<>(
                new UserDto.MessageResponse("Category deleted successfully"),
                HttpStatus.OK
        );
    }
}
