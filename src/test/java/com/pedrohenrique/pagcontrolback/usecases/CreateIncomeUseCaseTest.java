package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateIncomeCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.CreateIncomeCommandRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CreateIncomeUseCaseTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CreateIncomeUseCase createIncomeUseCase;


    @Nested
    class Success {

        @Test
        void shouldCreateIncomeSuccessfully() {

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            User user = new User();
            Category category = new Category("teste", TransactionType.INCOME, user);

            CreateIncomeCommand command = createCommand(userId, categoryId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                    .thenReturn(Optional.of(category));

            ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);

            createIncomeUseCase.execute(command);

            verify(incomeRepository).save(captor.capture());

            Income saved = captor.getValue();

            assertEquals("Salary", saved.getDescription());
            assertEquals(BigDecimal.valueOf(1000.00), saved.getAmount());
            assertEquals(category, saved.getCategory());
        }

        @Test
        void shouldCreateIncomeWithoutCategory() {
            UUID userId = UUID.randomUUID();

            CreateIncomeCommand command = createCommand(userId, null);

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(new User()));

            ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);

            createIncomeUseCase.execute(command);

            verify(incomeRepository).save(captor.capture());
            verify(categoryRepository, never()).findCategoryByIdAndUserId(any(), any());

            Income saved = captor.getValue();

            assertEquals("Salary", saved.getDescription());
            assertEquals(BigDecimal.valueOf(1000.00), saved.getAmount());
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldThrowCreateIncomeCommandRequiredExceptionWhenCommandIsNull() {

            assertThrows(CreateIncomeCommandRequiredException.class,
                    () -> createIncomeUseCase.execute(null));

            verify(incomeRepository, never()).save(any());
        }

        @Test
        void shouldThrowUserRequiredExceptionWhenUserIdIsNull() {

            CreateIncomeCommand command = createCommand(null, null);

            assertThrows(UserRequiredException.class,
                    () -> createIncomeUseCase.execute(command));

            verify(incomeRepository, never()).save(any());
        }

        @Test
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {

            UUID userId = UUID.randomUUID();
            CreateIncomeCommand command = createCommand(userId, null);

            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> createIncomeUseCase.execute(command));

            verify(incomeRepository, never()).save(any());
        }

        @Test
        void shouldThrowCategoryNotFoundExceptionWhenCategoryNotFound() {

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            CreateIncomeCommand command = createCommand(userId, categoryId);

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(new User()));

            when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class,
                    () -> createIncomeUseCase.execute(command));

            verify(incomeRepository, never()).save(any());
        }
    }

    private CreateIncomeCommand createCommand(UUID userId, UUID categoryId) {
        return new CreateIncomeCommand(
                BigDecimal.valueOf(1000.00),
                "Salary",
                LocalDate.now(),
                userId,
                categoryId
        );
    }
}