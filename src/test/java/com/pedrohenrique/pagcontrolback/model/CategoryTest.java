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
}