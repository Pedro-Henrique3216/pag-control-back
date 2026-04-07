package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.response.CategorySummaryDto;
import com.pedrohenrique.pagcontrolback.dtos.response.DashboardResponseDto;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDashboardUseCaseTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @InjectMocks
    private GetDashboardUseCase getDashboardUseCase;

    @Nested
    class Success {

        @Test
        void shouldReturnDashboardDataSuccessfully() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(3000));

            when(installmentRepository.sumPaidByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(2000));

            List<CategorySummaryDto> categories = List.of(
                    new CategorySummaryDto("Food", BigDecimal.valueOf(1500)),
                    new CategorySummaryDto("Outros", BigDecimal.valueOf(500))
            );

            when(installmentRepository.sumByCategory(any(), any(), any()))
                    .thenReturn(categories);

            DashboardResponseDto response =
                    getDashboardUseCase.execute(userId, month);

            assertEquals(BigDecimal.valueOf(3000), response.totalIncome());
            assertEquals(BigDecimal.valueOf(2000), response.totalExpense());
            assertEquals(BigDecimal.valueOf(1000), response.balance());
            assertEquals(2, response.expensesByCategory().size());
        }

        @Test
        void shouldCalculateNegativeBalance() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1000));

            when(installmentRepository.sumPaidByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1500));

            when(installmentRepository.sumByCategory(any(), any(), any()))
                    .thenReturn(List.of());

            DashboardResponseDto response =
                    getDashboardUseCase.execute(userId, month);

            assertEquals(BigDecimal.valueOf(-500), response.balance());
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void shouldReturnZeroWhenRepositoriesReturnNull() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(null);

            when(installmentRepository.sumPaidByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(null);

            when(installmentRepository.sumByCategory(any(), any(), any()))
                    .thenReturn(List.of());

            DashboardResponseDto response =
                    getDashboardUseCase.execute(userId, month);

            assertEquals(BigDecimal.ZERO, response.totalIncome());
            assertEquals(BigDecimal.ZERO, response.totalExpense());
            assertEquals(BigDecimal.ZERO, response.balance());
        }

        @Test
        void shouldReturnEmptyCategoryList() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1000));

            when(installmentRepository.sumPaidByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(500));

            when(installmentRepository.sumByCategory(any(), any(), any()))
                    .thenReturn(List.of());

            DashboardResponseDto response =
                    getDashboardUseCase.execute(userId, month);

            assertEquals(0, response.expensesByCategory().size());
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldCallRepositoriesWithCorrectDates() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            when(installmentRepository.sumPaidByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            when(installmentRepository.sumByCategory(any(), any(), any()))
                    .thenReturn(List.of());

            getDashboardUseCase.execute(userId, month);

            verify(incomeRepository).sumByUserIdAndDateBetween(
                    eq(userId),
                    eq(LocalDate.of(2026, 2, 1)),
                    eq(LocalDate.of(2026, 2, 28))
            );

            verify(installmentRepository).sumPaidByUserIdAndDateBetween(
                    eq(userId),
                    eq(LocalDate.of(2026, 2, 1)),
                    eq(LocalDate.of(2026, 2, 28))
            );

            verify(installmentRepository).sumByCategory(
                    eq(userId),
                    eq(LocalDate.of(2026, 2, 1)),
                    eq(LocalDate.of(2026, 2, 28))
            );
        }
    }
}