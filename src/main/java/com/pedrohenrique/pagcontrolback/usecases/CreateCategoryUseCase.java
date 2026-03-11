package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.CategoryAlreadyExistsException;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public Category execute(Category category, UUID userId) {
        if (category == null) {
            throw new CategoryRequiredException("Category cannot be null.");
        }

        if (userId == null) {
            throw new UserRequiredException("User ID cannot be null.");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if(categoryRepository.existsCategoryByNameAndUser_Id(category.getName(), userId)) {
            throw new CategoryAlreadyExistsException("Category with the same name already exists");
        }

        category.setUser(user);
        return categoryRepository.save(category);
    }
}
