package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateIncomeCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.CreateIncomeCommandRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateIncomeUseCase {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public CreateIncomeUseCase(IncomeRepository incomeRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Income execute(CreateIncomeCommand command) {
        if (command == null) {
            throw new CreateIncomeCommandRequiredException("Create income command cannot be null.");
        }

        if (command.userId() == null) {
            throw new UserRequiredException("User ID cannot be null.");
        }

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + command.userId()));

        Income income = new Income(
                command.amount(),
                command.description(),
                command.date(),
                user
        );

        if (command.categoryId() != null) {
            Category category = categoryRepository.findCategoryByIdAndUserId(command.categoryId(), command.userId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + command.categoryId()));

            income.assignCategory(category);
        }

        return incomeRepository.save(income);
    }
}
