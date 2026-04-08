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

    private void mockDefaults() {
        when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        when(installmentRepository.sumPaidByUserIdAndDateBetween(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        when(installmentRepository.sumOverdueByUser(any()))
                .thenReturn(BigDecimal.ZERO);

        when(installmentRepository.countOverdueByUser(any()))
                .thenReturn(0);

        when(installmentRepository.sumUpcomingByUser(any(), any()))
                .thenReturn(BigDecimal.ZERO);

        when(installmentRepository.countUpcomingByUser(any(), any()))
                .thenReturn(0);

        when(installmentRepository.sumByCategory(any(), any(), any()))
                .thenReturn(List.of());
    }

    @Nested
    class Success {

        @Test
        void shouldReturnDashboardDataSuccessfully() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            mockDefaults();

            when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(3000));

            when(installmentRepository.sumPaidByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(2000));

            when(installmentRepository.sumOverdueByUser(any()))
                    .thenReturn(BigDecimal.valueOf(300));

            when(installmentRepository.countOverdueByUser(any()))
                    .thenReturn(2);

            when(installmentRepository.sumUpcomingByUser(any(), any()))
                    .thenReturn(BigDecimal.valueOf(400));

            when(installmentRepository.countUpcomingByUser(any(), any()))
                    .thenReturn(3);

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
            assertEquals(BigDecimal.valueOf(300), response.overdueTotal());
            assertEquals(2, response.overdueCount());
            assertEquals(BigDecimal.valueOf(400), response.upcomingTotal());
            assertEquals(3, response.upcomingCount());
            assertEquals(2, response.expensesByCategory().size());
        }

        @Test
        void shouldCalculateNegativeBalance() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            mockDefaults();

            when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1000));

            when(installmentRepository.sumPaidByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1500));

            DashboardResponseDto response =
                    getDashboardUseCase.execute(userId, month);

            assertEquals(BigDecimal.valueOf(-500), response.balance());
        }

        @Test
        void shouldReturnUpcomingValuesCorrectly() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            mockDefaults();

            when(installmentRepository.sumUpcomingByUser(any(), any()))
                    .thenReturn(BigDecimal.valueOf(500));

            when(installmentRepository.countUpcomingByUser(any(), any()))
                    .thenReturn(3);

            DashboardResponseDto response =
                    getDashboardUseCase.execute(userId, month);

            assertEquals(BigDecimal.valueOf(500), response.upcomingTotal());
            assertEquals(3, response.upcomingCount());
        }

        @Test
        void shouldHandleDecimalValuesCorrectly() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            mockDefaults();

            when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(new BigDecimal("1000.55"));

            when(installmentRepository.sumPaidByUserIdAndDateBetween(any(), any(), any()))
                    .thenReturn(new BigDecimal("500.25"));

            DashboardResponseDto response =
                    getDashboardUseCase.execute(userId, month);

            assertEquals(new BigDecimal("500.30"), response.balance());
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

            when(installmentRepository.sumOverdueByUser(any()))
                    .thenReturn(null);

            when(installmentRepository.countOverdueByUser(any()))
                    .thenReturn(null);

            when(installmentRepository.sumUpcomingByUser(any(), any()))
                    .thenReturn(null);

            when(installmentRepository.countUpcomingByUser(any(), any()))
                    .thenReturn(null);

            when(installmentRepository.sumByCategory(any(), any(), any()))
                    .thenReturn(null);

            DashboardResponseDto response =
                    getDashboardUseCase.execute(userId, month);

            assertEquals(BigDecimal.ZERO, response.totalIncome());
            assertEquals(BigDecimal.ZERO, response.totalExpense());
            assertEquals(BigDecimal.ZERO, response.balance());
            assertEquals(BigDecimal.ZERO, response.overdueTotal());
            assertEquals(0, response.overdueCount());
            assertEquals(BigDecimal.ZERO, response.upcomingTotal());
            assertEquals(0, response.upcomingCount());
            assertEquals(0, response.expensesByCategory().size());
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldCallRepositoriesWithCorrectDates() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            mockDefaults();

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

        @Test
        void shouldCallOverdueMethods() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            mockDefaults();

            getDashboardUseCase.execute(userId, month);

            verify(installmentRepository).sumOverdueByUser(userId);
            verify(installmentRepository).countOverdueByUser(userId);
        }

        @Test
        void shouldCallUpcomingMethodsWithCorrectDate() {
            UUID userId = UUID.randomUUID();
            YearMonth month = YearMonth.of(2026, 2);

            mockDefaults();

            getDashboardUseCase.execute(userId, month);

            LocalDate expectedDate = LocalDate.now().plusDays(7);

            verify(installmentRepository).sumUpcomingByUser(eq(userId), eq(expectedDate));
            verify(installmentRepository).countUpcomingByUser(eq(userId), eq(expectedDate));
        }
    }
}