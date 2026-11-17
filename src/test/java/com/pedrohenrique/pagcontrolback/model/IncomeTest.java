package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class IncomeTest {

    private final Money validAmount = Money.of(BigDecimal.valueOf(1000));
    private final String validDescription = "Salary";
    private final LocalDate validDate = LocalDate.now();

    private Income createIncome() {
        return new Income(
                validAmount,
                validDescription,
                validDate,
                new User()
        );
    }

    @Nested
    class ConstructorTests {

        @Test
        void shouldCreateIncomeSuccessfully() {
            User user = new User();

            Income income = new Income(
                    validAmount,
                    validDescription,
                    validDate,
                    user
            );

            assertEquals(validAmount, income.getAmount());
            assertEquals(validDescription, income.getDescription());
            assertEquals(validDate, income.getDate());
            assertEquals(user, income.getUser());
            assertNotNull(income.getCreatedAt());
            assertNull(income.getUpdatedAt());
            assertNull(income.getCategory());
        }

        @Test
        void shouldThrowWhenAmountIsNull() {
            assertThrows(
                    InvalidAmountException.class,
                    () -> new Income(
                            null,
                            validDescription,
                            validDate,
                            new User()
                    )
            );
        }

        @Test
        void shouldThrowWhenDescriptionIsNull() {
            assertThrows(
                    DescriptionRequiredException.class,
                    () -> new Income(
                            validAmount,
                            null,
                            validDate,
                            new User()
                    )
            );
        }

        @Test
        void shouldThrowWhenDescriptionIsBlank() {
            assertThrows(
                    DescriptionRequiredException.class,
                    () -> new Income(
                            validAmount,
                            "   ",
                            validDate,
                            new User()
                    )
            );
        }

        @Test
        void shouldThrowWhenDescriptionExceeds100Characters() {
            String description = "A".repeat(101);

            assertThrows(
                    DescriptionInvalidException.class,
                    () -> new Income(
                            validAmount,
                            description,
                            validDate,
                            new User()
                    )
            );
        }

        @Test
        void shouldThrowWhenDateIsNull() {
            assertThrows(
                    DateRequiredException.class,
                    () -> new Income(
                            validAmount,
                            validDescription,
                            null,
                            new User()
                    )
            );
        }

        @Test
        void shouldThrowWhenUserIsNull() {
            assertThrows(
                    UserRequiredException.class,
                    () -> new Income(
                            validAmount,
                            validDescription,
                            validDate,
                            null
                    )
            );
        }
    }

    @Nested
    class SetUserTests {

        @Test
        void shouldSetUserSuccessfully() {
            Income income = createIncome();
            User user = new User();

            income.setUser(user);

            assertEquals(user, income.getUser());
        }

        @Test
        void shouldThrowWhenUserIsNull() {
            Income income = createIncome();

            assertThrows(
                    UserRequiredException.class,
                    () -> income.setUser(null)
            );
        }
    }

    @Nested
    class AssignCategoryTests {

        @Test
        void shouldAssignIncomeCategorySuccessfully() {
            Income income = createIncome();

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
            Income income = createIncome();

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

        @Test
        void shouldThrowWhenCategoryIsNull() {
            Income income = createIncome();

            assertThrows(
                    CategoryRequiredException.class,
                    () -> income.assignCategory(null)
            );
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateIncomeSuccessfully() {
            Income income = createIncome();

            Money newAmount = Money.of(BigDecimal.valueOf(2000));
            String newDescription = "Freelance";
            LocalDate newDate = LocalDate.now().minusDays(1);

            Category category = new Category(
                    "Services",
                    TransactionType.INCOME,
                    new User()
            );

            income.update(
                    newAmount,
                    newDescription,
                    newDate,
                    category
            );

            assertEquals(newAmount, income.getAmount());
            assertEquals(newDescription, income.getDescription());
            assertEquals(newDate, income.getDate());
            assertEquals(category, income.getCategory());
            assertNotNull(income.getUpdatedAt());
        }

        @Test
        void shouldThrowWhenUpdatingWithNullAmount() {
            Income income = createIncome();

            assertThrows(
                    InvalidAmountException.class,
                    () -> income.update(
                            null,
                            "Freelance",
                            validDate,
                            new Category(
                                    "Services",
                                    TransactionType.INCOME,
                                    new User()
                            )
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatingWithNullDescription() {
            Income income = createIncome();

            assertThrows(
                    DescriptionRequiredException.class,
                    () -> income.update(
                            validAmount,
                            null,
                            validDate,
                            new Category(
                                    "Services",
                                    TransactionType.INCOME,
                                    new User()
                            )
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatingWithBlankDescription() {
            Income income = createIncome();

            assertThrows(
                    DescriptionRequiredException.class,
                    () -> income.update(
                            validAmount,
                            "   ",
                            validDate,
                            new Category(
                                    "Services",
                                    TransactionType.INCOME,
                                    new User()
                            )
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatingWithDescriptionExceeding100Characters() {
            Income income = createIncome();

            assertThrows(
                    DescriptionInvalidException.class,
                    () -> income.update(
                            validAmount,
                            "A".repeat(101),
                            validDate,
                            new Category(
                                    "Services",
                                    TransactionType.INCOME,
                                    new User()
                            )
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatingWithNullDate() {
            Income income = createIncome();

            assertThrows(
                    DateRequiredException.class,
                    () -> income.update(
                            validAmount,
                            "Freelance",
                            null,
                            new Category(
                                    "Services",
                                    TransactionType.INCOME,
                                    new User()
                            )
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatingWithExpenseCategory() {
            Income income = createIncome();

            Category category = new Category(
                    "Food",
                    TransactionType.EXPENSE,
                    new User()
            );

            assertThrows(
                    CategoryTypeInvalidException.class,
                    () -> income.update(
                            validAmount,
                            "Freelance",
                            validDate,
                            category
                    )
            );
        }

        @Test
        void shouldThrowWhenUpdatingWithNullCategory() {
            Income income = createIncome();

            assertThrows(
                    CategoryRequiredException.class,
                    () -> income.update(
                            validAmount,
                            "Freelance",
                            validDate,
                            null
                    )
            );
        }
    }

    @Nested
    class EqualityTests {

        @Test
        void shouldBeEqualToItself() {
            Income income = createIncome();

            assertEquals(income, income);
        }

        @Test
        void shouldNotBeEqualToNull() {
            Income income = createIncome();

            assertNotEquals(null, income);
        }

        @Test
        void shouldNotBeEqualToDifferentObject() {
            Income income = createIncome();

            assertNotEquals(income, new User());
        }

        @Test
        void shouldHaveSameHashCodeWhenComparingSameObject() {
            Income income = createIncome();

            assertEquals(income.hashCode(), income.hashCode());
        }
    }
}
