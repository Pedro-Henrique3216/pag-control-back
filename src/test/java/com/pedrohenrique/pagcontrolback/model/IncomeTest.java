package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryTypeInvalidException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class IncomeTest {

    @Nested
    class ConstructorTests {

        @Test
        void shouldCreateIncomeSuccessfully() {
            User user = new User();

            Income income = new Income(
                    Money.of(BigDecimal.valueOf(1000)),
                    "Salary",
                    LocalDate.now(),
                    user
            );

            assertEquals(
                    Money.of(BigDecimal.valueOf(1000)),
                    income.getAmount()
            );

            assertEquals("Salary", income.getDescription());
            assertEquals(user, income.getUser());
            assertNotNull(income.getCreatedAt());
        }

        @Test
        void shouldThrowWhenUserIsNull() {
            assertThrows(
                    UserRequiredException.class,
                    () -> new Income(
                            Money.of(BigDecimal.valueOf(1000)),
                            "Salary",
                            LocalDate.now(),
                            null
                    )
            );
        }
    }

    @Nested
    class AssignCategoryTests {

        @Test
        void shouldAssignIncomeCategorySuccessfully() {
            Income income = new Income(
                    Money.of(BigDecimal.valueOf(1000)),
                    "Salary",
                    LocalDate.now(),
                    new User()
            );

            Category category = new Category(
                    "Salary",
                    TransactionType.INCOME,
                    new User()
            );

            income.assignCategory(category);

            assertEquals(category, income.getCategory());
        }

        @Test
        void shouldThrowWhenCategoryIsExpenseType() {
            Income income = new Income(
                    Money.of(BigDecimal.valueOf(1000)),
                    "Salary",
                    LocalDate.now(),
                    new User()
            );

            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            assertThrows(
                    CategoryTypeInvalidException.class,
                    () -> income.assignCategory(category)
            );
        }
    }

    @Nested
    class EqualityTests {

        @Test
        void shouldBeEqualToItself() {
            Income income = new Income(
                    Money.of(BigDecimal.valueOf(1000)),
                    "Salary",
                    LocalDate.now(),
                    new User()
            );

            assertEquals(income, income);
        }

        @Test
        void shouldNotBeEqualToNull() {
            Income income = new Income(
                    Money.of(BigDecimal.valueOf(1000)),
                    "Salary",
                    LocalDate.now(),
                    new User()
            );

            assertNotEquals(null, income);
        }
    }
}