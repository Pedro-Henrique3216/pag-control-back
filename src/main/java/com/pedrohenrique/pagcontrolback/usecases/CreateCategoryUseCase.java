package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateCategoryCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryAlreadyExistsException;
import com.pedrohenrique.pagcontrolback.exceptions.CreateCategoryCommandRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public Category execute(CreateCategoryCommand command) {
        if (command == null) {
            throw new CreateCategoryCommandRequiredException("Category create command cannot be null.");
        }

        if (command.userId() == null) {
            throw new UserRequiredException("User ID cannot be null.");
        }

        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + command.userId()));

        if(categoryRepository.existsCategoryByNameIgnoreCaseAndUserId(command.name(), command.userId())) {
            throw new CategoryAlreadyExistsException("Category with the same name already exists");
        }

        Category category = new Category(
                command.name(),
                command.categoryType(),
                user
        );
        return categoryRepository.save(category);
    }
}
