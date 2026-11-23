package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateIncomeCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.IncomeNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.model.TransactionType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.IncomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateIncomeUseCaseTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private UpdateIncomeUseCase updateIncomeUseCase;

    private UUID incomeId;
    private UUID userId;
    private UUID categoryId;

    private Income income;
    private Category category;

    @BeforeEach
    void setUp() {
        incomeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        User user = new User();

        category = new Category(
                "Salary",
                TransactionType.INCOME,
                user
        );

        income = new Income(
                com.pedrohenrique.pagcontrolback.ValueObjects.Money.of(
                        BigDecimal.valueOf(1000)
                ),
                "Salary",
                LocalDate.now(),
                user
        );

        income.assignCategory(category);
    }

    private UpdateIncomeCommand createCommand(
            BigDecimal amount,
            String description,
            LocalDate date,
            UUID categoryId
    ) {
        return new UpdateIncomeCommand(
                amount,
                description,
                date,
                categoryId
        );
    }

    @Nested
    class ValidationTests {

        @Test
        void shouldThrowWhenCommandIsNull() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> updateIncomeUseCase.updateIncome(
                            null,
                            incomeId,
                            userId
                    )
            );

            verifyNoInteractions(incomeRepository, categoryRepository);
        }

        @Test
        void shouldThrowWhenIncomeIdIsNull() {
            UpdateIncomeCommand command = createCommand(
                    null,
                    null,
                    null,
                    null
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> updateIncomeUseCase.updateIncome(
                            command,
                            null,
                            userId
                    )
            );

            verifyNoInteractions(incomeRepository, categoryRepository);
        }

        @Test
        void shouldThrowWhenUserIdIsNull() {
            UpdateIncomeCommand command = createCommand(
                    null,
                    null,
                    null,
                    null
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> updateIncomeUseCase.updateIncome(
                            command,
                            incomeId,
                            null
                    )
            );

            verifyNoInteractions(incomeRepository, categoryRepository);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateIncomeSuccessfully() {
            BigDecimal newAmount = BigDecimal.valueOf(2000);
            String newDescription = "Freelance";
            LocalDate newDate = LocalDate.now().minusDays(1);

            Category newCategory = new Category(
                    "Services",
                    TransactionType.INCOME,
                    new User()
            );

            UpdateIncomeCommand command = createCommand(
                    newAmount,
                    newDescription,
                    newDate,
                    categoryId
            );

            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.of(income));

            when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                    .thenReturn(Optional.of(newCategory));

            when(incomeRepository.save(income))
                    .thenReturn(income);

            Income result = updateIncomeUseCase.updateIncome(
                    command,
                    incomeId,
                    userId
            );

            assertNotNull(result);
            assertEquals(newAmount.setScale(2, RoundingMode.HALF_UP), result.getAmount().value());
            assertEquals(newDescription, result.getDescription());
            assertEquals(newDate, result.getDate());
            assertEquals(newCategory, result.getCategory());
            assertNotNull(result.getUpdatedAt());

            verify(incomeRepository).findIncomeByIdAndUserId(incomeId, userId);
            verify(categoryRepository).findCategoryByIdAndUserId(categoryId, userId);
            verify(incomeRepository).save(income);
        }

        @Test
        void shouldUpdateOnlyAmount() {
            BigDecimal newAmount = BigDecimal.valueOf(2500);

            UpdateIncomeCommand command = createCommand(
                    newAmount,
                    null,
                    null,
                    null
            );

            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.of(income));

            when(incomeRepository.save(income))
                    .thenReturn(income);

            Income result = updateIncomeUseCase.updateIncome(
                    command,
                    incomeId,
                    userId
            );

            assertEquals(newAmount.setScale(2, RoundingMode.HALF_UP), result.getAmount().value());
            assertEquals("Salary", result.getDescription());
            assertEquals(LocalDate.now(), result.getDate());
            assertEquals(category, result.getCategory());

            verify(categoryRepository, never())
                    .findCategoryByIdAndUserId(any(), any());

            verify(incomeRepository).save(income);
        }

        @Test
        void shouldUpdateOnlyDescription() {
            UpdateIncomeCommand command = createCommand(
                    null,
                    "Freelance",
                    null,
                    null
            );

            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.of(income));

            when(incomeRepository.save(income))
                    .thenReturn(income);

            Income result = updateIncomeUseCase.updateIncome(
                    command,
                    incomeId,
                    userId
            );

            assertEquals("Salary", result.getDescription().equals("Freelance")
                    ? "Salary" : result.getDescription());
            assertEquals("Freelance", result.getDescription());
            assertEquals(
                    com.pedrohenrique.pagcontrolback.ValueObjects.Money.of(
                            BigDecimal.valueOf(1000)
                    ),
                    result.getAmount()
            );
            assertEquals(category, result.getCategory());

            verify(incomeRepository).save(income);
        }

        @Test
        void shouldUpdateOnlyDate() {
            LocalDate newDate = LocalDate.now().minusDays(5);

            UpdateIncomeCommand command = createCommand(
                    null,
                    null,
                    newDate,
                    null
            );

            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.of(income));

            when(incomeRepository.save(income))
                    .thenReturn(income);

            Income result = updateIncomeUseCase.updateIncome(
                    command,
                    incomeId,
                    userId
            );

            assertEquals(newDate, result.getDate());
            assertEquals("Salary", result.getDescription());
            assertEquals(category, result.getCategory());

            verify(incomeRepository).save(income);
        }

        @Test
        void shouldPreserveCategoryWhenCategoryIdIsNull() {
            UpdateIncomeCommand command = createCommand(
                    BigDecimal.valueOf(1500),
                    "New salary",
                    LocalDate.now(),
                    null
            );

            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.of(income));

            when(incomeRepository.save(income))
                    .thenReturn(income);

            Income result = updateIncomeUseCase.updateIncome(
                    command,
                    incomeId,
                    userId
            );

            assertEquals(category, result.getCategory());

            verify(categoryRepository, never())
                    .findCategoryByIdAndUserId(any(), any());
        }
    }

    @Nested
    class NotFoundTests {

        @Test
        void shouldThrowWhenIncomeDoesNotExist() {
            UpdateIncomeCommand command = createCommand(
                    BigDecimal.valueOf(2000),
                    "Freelance",
                    LocalDate.now(),
                    null
            );

            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    IncomeNotFoundException.class,
                    () -> updateIncomeUseCase.updateIncome(
                            command,
                            incomeId,
                            userId
                    )
            );

            verify(incomeRepository, never()).save(any());
            verifyNoInteractions(categoryRepository);
        }

        @Test
        void shouldThrowWhenCategoryDoesNotExist() {
            UpdateIncomeCommand command = createCommand(
                    BigDecimal.valueOf(2000),
                    "Freelance",
                    LocalDate.now(),
                    categoryId
            );

            when(incomeRepository.findIncomeByIdAndUserId(incomeId, userId))
                    .thenReturn(Optional.of(income));

            when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    CategoryNotFoundException.class,
                    () -> updateIncomeUseCase.updateIncome(
                            command,
                            incomeId,
                            userId
                    )
            );

            verify(incomeRepository, never()).save(any());
        }
    }
}
