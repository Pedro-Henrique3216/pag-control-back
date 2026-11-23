package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ListIncomesUseCase {

    private final IncomeRepository incomeRepository;

    public ListIncomesUseCase(IncomeRepository incomeRepository){
        this.incomeRepository = incomeRepository;
    }

    public List<Income> listIncomes(UUID userId, LocalDate date){
        if (userId == null){
            throw new IllegalArgumentException("User id is required.");
        }

        if (date == null){
            return incomeRepository.findAllIncomeByUserId(userId);
        }

        return incomeRepository.findAllIncomeByUserIdAndDate(userId, date);
    }
}
