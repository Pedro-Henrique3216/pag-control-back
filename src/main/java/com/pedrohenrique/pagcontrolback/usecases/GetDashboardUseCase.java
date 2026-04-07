package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.response.CategorySummaryDto;
import com.pedrohenrique.pagcontrolback.dtos.response.DashboardResponseDto;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class GetDashboardUseCase {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public GetDashboardUseCase(IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    public DashboardResponseDto execute(UUID userId, YearMonth month) {

        LocalDate startMonth = month.atDay(1);
        LocalDate endMonth = month.atEndOfMonth();

        BigDecimal totalIncome = incomeRepository.sumByUserIdAndDateBetween(userId, startMonth, endMonth);
        BigDecimal totalExpense = expenseRepository.sumPaidByUserIdAndDateBetween(userId, startMonth, endMonth);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        List<CategorySummaryDto> byCategory = expenseRepository.sumByCategory(userId, startMonth, endMonth);
        System.out.println(byCategory);

        return new DashboardResponseDto(
                totalIncome,
                totalExpense,
                balance,
                byCategory);

    }

}
