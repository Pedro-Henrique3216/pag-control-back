package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.exceptions.InvalidInstallmentFilterException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepositoryCustom;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListInstallmentsUseCaseTest {

    @Mock
    private InstallmentRepositoryCustom installmentRepositoryCustom;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ListInstallmentsUseCase listInstallmentsUseCase;

    @Nested
    class ValidationTests {

        @Test
        void shouldThrowUserIdRequiredExceptionWhenUserIdIsNull() {

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    null,
                    null
            );

            var exception = assertThrows(
                    UserIdRequiredException.class,
                    () -> listInstallmentsUseCase.execute(null, query)
            );

            assertEquals(
                    "User id is required.",
                    exception.getMessage()
            );
        }

        @Test
        void shouldThrowInvalidInstallmentFilterExceptionWhenStatusAndOverdueAreUsedTogether() {

            UUID userId = UUID.randomUUID();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    InstallmentStatus.UNPAID,
                    true,
                    false,
                    null,
                    null
            );

            var exception = assertThrows(
                    InvalidInstallmentFilterException.class,
                    () -> listInstallmentsUseCase.execute(userId, query)
            );

            assertEquals(
                    "Cannot use status and overdue filters together.",
                    exception.getMessage()
            );
        }

        @Test
        void shouldThrowInvalidInstallmentFilterExceptionWhenCategoryIsNotExpense() {

            UUID userId = UUID.randomUUID();

            UUID categoryId = UUID.randomUUID();

            User user = mock(User.class);

            Category category = new Category(
                    "Salary",
                    TransactionType.INCOME,
                    user
            );

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    null,
                    categoryId
            );

            when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                    .thenReturn(Optional.of(category));

            var exception = assertThrows(
                    InvalidInstallmentFilterException.class,
                    () -> listInstallmentsUseCase.execute(userId, query)
            );

            assertEquals(
                    "Category must be of type EXPENSE.",
                    exception.getMessage()
            );
        }
    }

    @Nested
    class SuccessTests {

        @Test
        void shouldSearchInstallmentsWithAllFilters() {

            UUID userId = UUID.randomUUID();

            var query = new ListInstallmentQuery(
                    "mercado",
                    java.time.YearMonth.now(),
                    UUID.randomUUID(),
                    InstallmentStatus.UNPAID,
                    false,
                    true,
                    PaymentType.CREDIT,
                    null
            );

            when(installmentRepositoryCustom.search(query, userId))
                    .thenReturn(Collections.emptyList());

            var result = listInstallmentsUseCase.execute(userId, query);

            assertNotNull(result);

            verify(installmentRepositoryCustom)
                    .search(query, userId);
        }

        @Test
        void shouldSearchInstallmentsWhenUserAndFiltersAreValid() {

            UUID userId = UUID.randomUUID();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    null,
                    null
            );

            when(installmentRepositoryCustom.search(query, userId))
                    .thenReturn(Collections.emptyList());

            var result = listInstallmentsUseCase.execute(userId, query);

            assertNotNull(result);

            verify(installmentRepositoryCustom)
                    .search(query, userId);
        }

        @Test
        void shouldSearchInstallmentsWhenCategoryIsExpense() {

            UUID userId = UUID.randomUUID();

            UUID categoryId = UUID.randomUUID();

            User user = mock(User.class);

            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    user
            );

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    null,
                    categoryId
            );

            when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                    .thenReturn(Optional.of(category));

            when(installmentRepositoryCustom.search(query, userId))
                    .thenReturn(Collections.emptyList());

            var result = listInstallmentsUseCase.execute(userId, query);

            assertNotNull(result);

            verify(categoryRepository)
                    .findCategoryByIdAndUserId(categoryId, userId);

            verify(installmentRepositoryCustom)
                    .search(query, userId);
        }

        @Test
        void shouldSearchInstallmentsWhenCategoryDoesNotExist() {

            UUID userId = UUID.randomUUID();

            UUID categoryId = UUID.randomUUID();

            var query = new ListInstallmentQuery(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    null,
                    categoryId
            );

            when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                    .thenReturn(Optional.empty());

            when(installmentRepositoryCustom.search(query, userId))
                    .thenReturn(Collections.emptyList());

            var result = listInstallmentsUseCase.execute(userId, query);

            assertNotNull(result);

            verify(categoryRepository)
                    .findCategoryByIdAndUserId(categoryId, userId);

            verify(installmentRepositoryCustom)
                    .search(query, userId);
        }
    }
}