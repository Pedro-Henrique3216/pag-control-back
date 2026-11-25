package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCategoryByIdUseCase {

    private final CategoryRepository categoryRepository;

    public GetCategoryByIdUseCase(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    public Category findCategoryById(UUID categoryId, UUID userId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("category id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("user id cannot be null");
        }

        return categoryRepository.findCategoryByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("category not found"));
    }

}
