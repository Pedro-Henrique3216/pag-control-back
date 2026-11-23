package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.IncomeNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeactivateIncomeUseCase {

    private final IncomeRepository incomeRepository;

    public DeactivateIncomeUseCase(
            IncomeRepository incomeRepository
    ) {
        this.incomeRepository = incomeRepository;
    }

    @Transactional
    public void deactivateIncome(UUID incomeId, UUID userId){

        if (incomeId == null) {
            throw new IllegalArgumentException("income id is required");
        }

        if (userId == null) {
            throw new IllegalArgumentException("user id is required");
        }

        Income income = incomeRepository.findIncomeByIdAndUserId(incomeId, userId)
                .orElseThrow(() -> new IncomeNotFoundException("income not found"));

        if (!income.isActive()){
            return;
        }

        income.deactivate();
        incomeRepository.save(income);
    }
}
