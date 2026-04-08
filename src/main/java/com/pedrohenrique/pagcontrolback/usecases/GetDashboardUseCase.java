package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.response.CategorySummaryDto;
import com.pedrohenrique.pagcontrolback.dtos.response.DashboardResponseDto;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GetDashboardUseCase {

    private final IncomeRepository incomeRepository;
    private final InstallmentRepository installmentRepository;

    public GetDashboardUseCase(IncomeRepository incomeRepository, InstallmentRepository installmentRepository) {
        this.incomeRepository = incomeRepository;
        this.installmentRepository = installmentRepository;
    }

    public DashboardResponseDto execute(UUID userId, YearMonth month) {

        LocalDate startMonth = month.atDay(1);
        LocalDate endMonth = month.atEndOfMonth();

        BigDecimal totalIncome = Optional.ofNullable(
                incomeRepository.sumByUserIdAndDateBetween(userId, startMonth, endMonth)
        ).orElse(BigDecimal.ZERO);

        BigDecimal totalExpense = Optional.ofNullable(
                installmentRepository.sumPaidByUserIdAndDateBetween(userId, startMonth, endMonth)
        ).orElse(BigDecimal.ZERO);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        BigDecimal totalOverdue = Optional.ofNullable(
                installmentRepository.sumOverdueByUser(userId)
        ).orElse(BigDecimal.ZERO);

        Integer overdueCount = Optional.ofNullable(installmentRepository.countOverdueByUser(userId))
                .orElse(0);

        LocalDate futureDate = LocalDate.now().plusDays(7);

        BigDecimal upcomingTotal = Optional.ofNullable(
                installmentRepository.sumUpcomingByUser(userId, futureDate)
        ).orElse(BigDecimal.ZERO);

        Integer upcomingCount = Optional.ofNullable(installmentRepository.countUpcomingByUser(userId, futureDate))
                .orElse(0);

        List<CategorySummaryDto> byCategory = installmentRepository.sumByCategory(userId, startMonth, endMonth);

        if (byCategory == null) {
            byCategory = new ArrayList<>();
        }

        return new DashboardResponseDto(
                totalIncome,
                totalExpense,
                balance,
                totalOverdue,
                overdueCount,
                upcomingTotal,
                upcomingCount,
                byCategory);

    }

}
