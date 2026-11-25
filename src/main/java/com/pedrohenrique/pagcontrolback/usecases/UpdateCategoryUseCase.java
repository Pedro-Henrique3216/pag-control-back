package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateCategoryCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public UpdateCategoryUseCase(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category updateCategory(UUID categoryId, UUID userId, UpdateCategoryCommand command) {
        if (categoryId == null) {
            throw new IllegalArgumentException("category id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("user id cannot be null");
        }

        if (command == null) {
            throw new IllegalArgumentException("update category command cannot be null");
        }

        Category category = categoryRepository.findCategoryByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("category not found"));

        category.update(
                command.name(),
                command.categoryType()
        );

        categoryRepository.save(category);

        return category;

    }
}
