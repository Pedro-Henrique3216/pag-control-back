package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.IncomeNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetIncomeByIdUseCaseTest {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private GetIncomeByIdUseCase getIncomeByIdUseCase;

    private UUID incomeId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        incomeId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void shouldReturnIncomeWhenIncomeExists() {
        Income income = mock(Income.class);

        when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                .thenReturn(Optional.of(income));

        Income result = getIncomeByIdUseCase.getIncomeById(incomeId, userId);

        assertNotNull(result);
        assertEquals(income, result);

        verify(incomeRepository).findIncomeByIdAndUserId(incomeId, userId);
    }

    @Test
    void shouldThrowExceptionWhenIncomeIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> getIncomeByIdUseCase.getIncomeById(null, userId)
        );

        verifyNoInteractions(incomeRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> getIncomeByIdUseCase.getIncomeById(incomeId, null)
        );

        verifyNoInteractions(incomeRepository);
    }

    @Test
    void shouldThrowIncomeNotFoundExceptionWhenIncomeDoesNotExist() {
        when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                IncomeNotFoundException.class,
                () -> getIncomeByIdUseCase.getIncomeById(incomeId, userId)
        );

        verify(incomeRepository).findIncomeByIdAndUserId(incomeId, userId);
    }
}
