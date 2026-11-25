package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeactivateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public DeactivateCategoryUseCase(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void deactivateCategory(UUID categoryId, UUID userId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("category id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("user id cannot be null");
        }

        Category category = categoryRepository.findCategoryByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("category not found"));

        category.deactivate();

        categoryRepository.save(category);
    }
}
