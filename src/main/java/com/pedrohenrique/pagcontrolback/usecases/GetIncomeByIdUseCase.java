package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.IncomeNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetIncomeByIdUseCase {

    private final IncomeRepository incomeRepository;

    public GetIncomeByIdUseCase(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public Income getIncomeById(UUID incomeId, UUID userId){
        if (incomeId == null){
            throw new IllegalArgumentException("income id cannot be null");
        }

        if (userId == null){
            throw new IllegalArgumentException("user id cannot be null");
        }

        return incomeRepository.findIncomeByIdAndUserId(incomeId, userId)
                .orElseThrow(() -> new IncomeNotFoundException("income not found"));
    }
}
