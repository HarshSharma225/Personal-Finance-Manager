package com.finance.manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class CategoryDto {

    public static class CreateCategoryRequest {

        @NotBlank(message = "Category name is mandatory")
        private String name;

        @NotBlank(message = "Category type is mandatory")
        @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Category type must be INCOME or EXPENSE")
        private String type;

        public CreateCategoryRequest() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class CategoryResponse {
        private String name;
        private String type;
        private boolean isCustom;
        private boolean custom; // for compatibility with the test script

        public CategoryResponse(String name, String type, boolean isCustom) {
            this.name = name;
            this.type = type;
            this.isCustom = isCustom;
            this.custom = isCustom;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean getIsCustom() {
            return isCustom;
        }

        public void setIsCustom(boolean custom) {
            isCustom = custom;
        }

        public boolean getCustom() {
            return custom;
        }

        public void setCustom(boolean custom) {
            this.custom = custom;
        }
    }

    public static class CategoryListResponse {
        private List<CategoryResponse> categories;

        public CategoryListResponse(List<CategoryResponse> categories) {
            this.categories = categories;
        }

        public List<CategoryResponse> getCategories() {
            return categories;
        }

        public void setCategories(List<CategoryResponse> categories) {
            this.categories = categories;
        }
    }
}
