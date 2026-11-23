
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListIncomesUseCaseTest {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private ListIncomesUseCase listIncomesUseCase;

    @Test
    void shouldListAllIncomesWhenDateIsNull() {
        UUID userId = UUID.randomUUID();

        List<Income> expectedIncomes = List.of(
                mock(Income.class),
                mock(Income.class)
        );

        when(incomeRepository.findAllIncomeByUserId(userId))
                .thenReturn(expectedIncomes);

        List<Income> result = listIncomesUseCase.listIncomes(userId, null);

        assertEquals(expectedIncomes, result);

        verify(incomeRepository).findAllIncomeByUserId(userId);
        verify(incomeRepository, never())
                .findAllIncomeByUserIdAndDate(any(), any());
    }

    @Test
    void shouldListIncomesByDateWhenDateIsProvided() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 11, 16);

        List<Income> expectedIncomes = List.of(
                mock(Income.class)
        );

        when(incomeRepository.findAllIncomeByUserIdAndDate(userId, date))
                .thenReturn(expectedIncomes);

        List<Income> result = listIncomesUseCase.listIncomes(userId, date);

        assertEquals(expectedIncomes, result);

        verify(incomeRepository)
                .findAllIncomeByUserIdAndDate(userId, date);

        verify(incomeRepository, never())
                .findAllIncomeByUserId(any());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        LocalDate date = LocalDate.now();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> listIncomesUseCase.listIncomes(null, date)
        );

        assertEquals("User id is required.", exception.getMessage());

        verifyNoInteractions(incomeRepository);
    }
}
