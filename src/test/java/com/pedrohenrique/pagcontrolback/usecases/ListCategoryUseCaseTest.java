package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.TransactionType;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ListCategoryUseCase listCategoryUseCase;

    @Nested
    class ListAllCategoriesTests {

        @Test
        void shouldReturnCategoriesWhenUserHasCategories() {

            Category category1 = new Category();
            Category category2 = new Category();
            Category category3 = new Category();

            when(categoryRepository.findCategoriesByUserId(any()))
                    .thenReturn(List.of(category1, category2, category3));

            List<Category> categories = listCategoryUseCase.execute(
                    UUID.randomUUID(),
                    null
            );

            assertEquals(3, categories.size());

            verify(categoryRepository, times(1))
                    .findCategoriesByUserId(any());
        }

        @Test
        void shouldReturnEmptyListWhenUserDoesNotHaveCategories() {

            when(categoryRepository.findCategoriesByUserId(any()))
                    .thenReturn(List.of());

            List<Category> categories = listCategoryUseCase.execute(
                    UUID.randomUUID(),
                    null
            );

            assertTrue(categories.isEmpty());
        }
    }

    @Nested
    class ListByCategoryTypeTests {

        @Test
        void shouldReturnCategoriesFilteredByType() {

            Category category1 = new Category();
            Category category2 = new Category();

            when(categoryRepository.findCategoryByUserIdAndCategoryType(
                    any(),
                    any()
            )).thenReturn(List.of(category1, category2));

            List<Category> categories = listCategoryUseCase.execute(
                    UUID.randomUUID(),
                    TransactionType.EXPENSE
            );

            assertEquals(2, categories.size());

            verify(categoryRepository, times(1))
                    .findCategoryByUserIdAndCategoryType(
                            any(),
                            eq(TransactionType.EXPENSE)
                    );
        }

        @Test
        void shouldReturnEmptyListWhenNoCategoriesExistForType() {

            when(categoryRepository.findCategoryByUserIdAndCategoryType(
                    any(),
                    any()
            )).thenReturn(List.of());

            List<Category> categories = listCategoryUseCase.execute(
                    UUID.randomUUID(),
                    TransactionType.INCOME
            );

            assertTrue(categories.isEmpty());
        }
    }

    @Nested
    class ValidationTests {

        @Test
        void shouldThrowUserRequiredExceptionWhenUserIdIsNull() {

            assertThrows(
                    UserRequiredException.class,
                    () -> listCategoryUseCase.execute(
                            null,
                            null
                    )
            );
        }
    }
}