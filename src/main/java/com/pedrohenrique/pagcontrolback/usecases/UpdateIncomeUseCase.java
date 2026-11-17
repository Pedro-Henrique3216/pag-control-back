package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateIncomeCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.IncomeNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UpdateIncomeUseCase {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;

    public UpdateIncomeUseCase(
            IncomeRepository incomeRepository,
            CategoryRepository categoryRepository
    ) {
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Income updateIncome(UpdateIncomeCommand command, UUID incomeId, UUID userId){

        if (command == null){
            throw new IllegalArgumentException("command is required");
        }

        if (incomeId == null){
            throw new IllegalArgumentException("income id is required");
        }

        if (userId == null){
            throw new IllegalArgumentException("user id is required");
        }

        Income income = incomeRepository.findIncomeByIdAndUserId(incomeId, userId)
                .orElseThrow(() -> new IncomeNotFoundException("income not found"));

        Money amount = command.amount() != null
                ? Money.of(command.amount())
                : income.getAmount();

        String description = command.description() != null
                ? command.description()
                : income.getDescription();

        LocalDate date = command.date() != null
                ? command.date()
                : income.getDate();


        UUID categoryId = command.categoryId();

        Category category = income.getCategory();

        if (categoryId != null){
            category = categoryRepository.findCategoryByIdAndUserId(categoryId, userId)
                    .orElseThrow(() -> new CategoryNotFoundException("category not found"));
        }

        income.update(
                amount,
                description,
                date,
                category
        );

        return incomeRepository.save(income);
    }
}
