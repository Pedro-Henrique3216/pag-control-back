package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.CategoryNameInvalidException;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryTypeInvalidException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Nested
    class CreationTests {

        @Test
        void shouldCreateCategorySuccessfully() {
            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            assertEquals("food", category.getName());
            assertEquals(TransactionType.EXPENSE, category.getCategoryType());
            assertNotNull(category.getCreatedAt());
            assertTrue(category.getActive());
        }

        @Test
        void shouldCreateIncomeCategorySuccessfully() {
            Category category = new Category(
                    "Salary",
                    TransactionType.INCOME,
                    new User()
            );

            assertEquals(TransactionType.INCOME, category.getCategoryType());
        }
    }

    @Nested
    class NameValidationTests {

        @Test
        void shouldThrowExceptionWhenNameIsNull() {
            assertThrows(
                    CategoryNameInvalidException.class,
                    () -> new Category(
                            null,
                            TransactionType.EXPENSE,
                            new User()
                    )
            );
        }

        @Test
        void shouldThrowExceptionWhenNameIsBlank() {
            assertThrows(
                    CategoryNameInvalidException.class,
                    () -> new Category(
                            "   ",
                            TransactionType.EXPENSE,
                            new User()
                    )
            );
        }
    }

    @Nested
    class CategoryTypeValidationTests {

        @Test
        void shouldThrowExceptionWhenCategoryTypeIsNull() {
            assertThrows(
                    CategoryTypeInvalidException.class,
                    () -> new Category(
                            "Food",
                            null,
                            new User()
                    )
            );
        }
    }

    @Nested
    class UserValidationTests {

        @Test
        void shouldThrowExceptionWhenUserIsNull() {
            assertThrows(
                    UserRequiredException.class,
                    () -> new Category(
                            "Food",
                            TransactionType.EXPENSE,
                            null
                    )
            );
        }
    }

    @Nested
    class EqualityTests {

        @Test
        void shouldBeEqualWhenComparingSameInstance() {
            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            assertEquals(category, category);
        }

        @Test
        void shouldNotBeEqualToNull() {
            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            assertNotEquals(null, category);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateCategorySuccessfully() {
            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            category.update("Salary", TransactionType.INCOME);

            assertEquals("salary", category.getName());
            assertEquals(TransactionType.INCOME, category.getCategoryType());
            assertNotNull(category.getUpdatedAt());
        }

        @Test
        void shouldNormalizeCategoryNameWhenUpdating() {
            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            category.update("  TRANSPORT  ", TransactionType.EXPENSE);

            assertEquals("transport", category.getName());
        }

        @Test
        void shouldThrowExceptionWhenUpdatingWithNullName() {
            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            assertThrows(
                    CategoryNameInvalidException.class,
                    () -> category.update(null, TransactionType.EXPENSE)
            );
        }

        @Test
        void shouldThrowExceptionWhenUpdatingWithBlankName() {
            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            assertThrows(
                    CategoryNameInvalidException.class,
                    () -> category.update("   ", TransactionType.EXPENSE)
            );
        }

        @Test
        void shouldThrowExceptionWhenUpdatingWithNullCategoryType() {
            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            assertThrows(
                    CategoryTypeInvalidException.class,
                    () -> category.update("Transport", null)
            );
        }

        @Test
        void shouldUpdateUpdatedAtWhenUpdatingCategory() {
            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            category.update("Transport", TransactionType.EXPENSE);

            assertNotNull(category.getUpdatedAt());
            assertFalse(category.getUpdatedAt().isBefore(category.getCreatedAt()));
        }
    }
}