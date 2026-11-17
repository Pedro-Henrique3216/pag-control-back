package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.IncomeNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeactivateIncomeUseCaseTest {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private DeactivateIncomeUseCase deactivateIncomeUseCase;

    private UUID incomeId;
    private UUID userId;
    private Income income;

    @BeforeEach
    void setUp() {
        incomeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        income = new Income(
                com.pedrohenrique.pagcontrolback.ValueObjects.Money.of(
                        BigDecimal.valueOf(1000)
                ),
                "Salary",
                LocalDate.now(),
                new com.pedrohenrique.pagcontrolback.model.User()
        );
    }

    @Nested
    class ValidationTests {

        @Test
        void shouldThrowWhenIncomeIdIsNull() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> deactivateIncomeUseCase.deactivateIncome(
                            null,
                            userId
                    )
            );

            verifyNoInteractions(incomeRepository);
        }

        @Test
        void shouldThrowWhenUserIdIsNull() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> deactivateIncomeUseCase.deactivateIncome(
                            incomeId,
                            null
                    )
            );

            verifyNoInteractions(incomeRepository);
        }
    }

    @Nested
    class DeactivateTests {

        @Test
        void shouldDeactivateIncomeSuccessfully() {
            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.of(income));

            deactivateIncomeUseCase.deactivateIncome(incomeId, userId);

            assertFalse(income.isActive());

            verify(incomeRepository).findIncomeByIdAndUserId(incomeId, userId);
            verify(incomeRepository).save(income);
        }

        @Test
        void shouldThrowWhenIncomeDoesNotExist() {
            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    IncomeNotFoundException.class,
                    () -> deactivateIncomeUseCase.deactivateIncome(
                            incomeId,
                            userId
                    )
            );

            verify(incomeRepository, never()).save(any());
        }

        @Test
        void shouldNotSaveWhenIncomeIsAlreadyInactive() {
            income.deactivate();

            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.of(income));

            deactivateIncomeUseCase.deactivateIncome(incomeId, userId);

            assertFalse(income.isActive());

            verify(incomeRepository).findIncomeByIdAndUserId(incomeId, userId);
            verify(incomeRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenIncomeBelongsToAnotherUser() {
            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    IncomeNotFoundException.class,
                    () -> deactivateIncomeUseCase.deactivateIncome(
                            incomeId,
                            userId
                    )
            );

            verify(incomeRepository).findIncomeByIdAndUserId(incomeId, userId);
            verify(incomeRepository, never()).save(any());
        }
    }
}
