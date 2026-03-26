package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Category;
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

    public List<Category> execute(UUID userId) {
        if(userId == null){
            throw new UserRequiredException("User ID is required to list categories.");
        }
        return categoryRepository.findCategoriesByUserId(userId);
    }
}
