package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.TransactionType;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public ListCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> execute(UUID userId, TransactionType categoryType) {
        if(userId == null){
            throw new UserRequiredException("User ID is required to list categories.");
        }

        if(categoryType != null) return categoryRepository.findCategoryByUserIdAndCategoryType(userId, categoryType);

        return categoryRepository.findCategoriesByUserId(userId);
    }
}
