package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("Should sum incomes by date range")
    void shouldSumIncomesByDateRange() {
        User user = userRepository.save(
                new User("Pedro", null, "email@test.com", "123", "11999999999", PersonType.PF)
        );

        incomeRepository.save(new Income(
                BigDecimal.valueOf(1000),
                "Salário",
                LocalDate.of(2026, 2, 10),
                user
        ));

        incomeRepository.save(new Income(
                BigDecimal.valueOf(2000),
                "Freelance",
                LocalDate.of(2026, 2, 20),
                user
        ));

        BigDecimal result = incomeRepository.sumByUserIdAndDateBetween(
                user.getId(),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertEquals(new BigDecimal("3000").floatValue(), result.floatValue());
    }

    @Test
    @DisplayName("Should return zero when no incomes")
    void shouldReturnZeroWhenNoIncomes() {
        User user = userRepository.save(
                new User("Pedro", null, "email@test.com", "123", "11999999999", PersonType.PF)
        );

        BigDecimal result = incomeRepository.sumByUserIdAndDateBetween(
                user.getId(),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("Should ignore incomes outside date range")
    void shouldIgnoreIncomesOutsideDateRange() {
        User user = userRepository.save(
                new User("Pedro", null, "email@test.com", "123", "11999999999", PersonType.PF)
        );

        incomeRepository.save(new Income(
                BigDecimal.valueOf(1000),
                "Janeiro",
                LocalDate.of(2026, 1, 10),
                user
        ));

        incomeRepository.save(new Income(
                BigDecimal.valueOf(2000),
                "Fevereiro",
                LocalDate.of(2026, 2, 10),
                user
        ));

        BigDecimal result = incomeRepository.sumByUserIdAndDateBetween(
                user.getId(),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertEquals(new BigDecimal("2000").floatValue(), result.floatValue());
    }

    @Test
    @DisplayName("Should sum only incomes from the correct user")
    void shouldSumOnlyFromCorrectUser() {
        User user1 = userRepository.save(
                new User("Pedro", null, "email1@test.com", "123", "11999999999", PersonType.PF)
        );

        User user2 = userRepository.save(
                new User("João", null, "email2@test.com", "123", "11888888888", PersonType.PF)
        );

        incomeRepository.save(new Income(
                BigDecimal.valueOf(1000),
                "User1 Income",
                LocalDate.of(2026, 2, 10),
                user1
        ));

        incomeRepository.save(new Income(
                BigDecimal.valueOf(5000),
                "User2 Income",
                LocalDate.of(2026, 2, 10),
                user2
        ));

        BigDecimal result = incomeRepository.sumByUserIdAndDateBetween(
                user1.getId(),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertEquals(new BigDecimal("1000").floatValue(), result.floatValue());
    }
}