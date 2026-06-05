package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class IncomeRepositoryTest {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser(
            String name,
            String email,
            String phone
    ) {

        User user = new User(
                name,
                null,
                email,
                "123",
                phone,
                PersonType.PF
        );

        return userRepository.save(user);
    }

    private Income createIncome(
            User user,
            BigDecimal amount,
            String description,
            LocalDate date
    ) {

        Income income = new Income(
                new Money(amount),
                description,
                date,
                user
        );

        return incomeRepository.save(income);
    }

    @Nested
    class SumByDateRangeTests {

        @Test
        @DisplayName("Should sum incomes by date range")
        void shouldSumIncomesByDateRange() {

            User user = createUser(
                    "Pedro",
                    "email@test.com",
                    "11999999999"
            );

            createIncome(
                    user,
                    BigDecimal.valueOf(1000),
                    "Salário",
                    LocalDate.of(2026, 2, 10)
            );

            createIncome(
                    user,
                    BigDecimal.valueOf(2000),
                    "Freelance",
                    LocalDate.of(2026, 2, 20)
            );

            BigDecimal result =
                    incomeRepository.sumByUserIdAndDateBetween(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(
                    new BigDecimal("3000").floatValue(),
                    result.floatValue()
            );
        }

        @Test
        @DisplayName("Should return zero when no incomes")
        void shouldReturnZeroWhenNoIncomes() {

            User user = createUser(
                    "Pedro",
                    "empty@test.com",
                    "11999999999"
            );

            BigDecimal result =
                    incomeRepository.sumByUserIdAndDateBetween(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(
                    BigDecimal.ZERO.floatValue(),
                    result.floatValue()
            );
        }

        @Test
        @DisplayName("Should ignore incomes outside date range")
        void shouldIgnoreIncomesOutsideDateRange() {

            User user = createUser(
                    "Pedro",
                    "range@test.com",
                    "11999999999"
            );

            createIncome(
                    user,
                    BigDecimal.valueOf(1000),
                    "Janeiro",
                    LocalDate.of(2026, 1, 10)
            );

            createIncome(
                    user,
                    BigDecimal.valueOf(2000),
                    "Fevereiro",
                    LocalDate.of(2026, 2, 10)
            );

            BigDecimal result =
                    incomeRepository.sumByUserIdAndDateBetween(
                            user.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(
                    new BigDecimal("2000").floatValue(),
                    result.floatValue()
            );
        }

        @Test
        @DisplayName("Should sum only incomes from the correct user")
        void shouldSumOnlyFromCorrectUser() {

            User user1 = createUser(
                    "Pedro",
                    "email1@test.com",
                    "11999999999"
            );

            User user2 = createUser(
                    "João",
                    "email2@test.com",
                    "11888888888"
            );

            createIncome(
                    user1,
                    BigDecimal.valueOf(1000),
                    "User1 Income",
                    LocalDate.of(2026, 2, 10)
            );

            createIncome(
                    user2,
                    BigDecimal.valueOf(5000),
                    "User2 Income",
                    LocalDate.of(2026, 2, 10)
            );

            BigDecimal result =
                    incomeRepository.sumByUserIdAndDateBetween(
                            user1.getId(),
                            LocalDate.of(2026, 2, 1),
                            LocalDate.of(2026, 2, 28)
                    );

            assertEquals(
                    new BigDecimal("1000").floatValue(),
                    result.floatValue()
            );
        }
    }
}