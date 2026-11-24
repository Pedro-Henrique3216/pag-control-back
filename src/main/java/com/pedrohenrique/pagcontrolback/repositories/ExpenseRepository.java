package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.usecases.ListCreditCardExpensesUseCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query(
            """
                SELECT EXISTS
                (
                 SELECT 1
                 FROM Expense e
                 WHERE e.recurringExpense.id = :recurringExpenseId
                 AND e.expenseDate = :date
                 )
            """
    )
    boolean existsByRecurringExpenseIdAndOccurrenceDate(UUID recurringExpenseId, LocalDate date);

    @Query(
            """
                SELECT e FROM Expense e
                WHERE e.id = :expenseId
                AND e.user.id = :userId
                AND e.active = true
            """
    )
    Optional<Expense> findActiveExpenseByIdAndUserId(UUID expenseId, UUID userId);

    @Query(
            """
                SELECT COALESCE(SUM(e.totalAmount.value), 0)
                FROM Expense e
                WHERE e.creditCard.id = :creditCardId
                AND e.user.id = :userId
                AND e.active = true
            """
    )
    BigDecimal findUsedLimitByCreditCardId(UUID creditCardId, UUID userId);

    @Query(
            """
                SELECT e
                FROM Expense e
                WHERE e.creditCard.id = :creditCardId
                AND e.user.id = :userId
                AND e.active = true
            """
    )
    List<Expense> findExpensesByCreditCardIdAndUserId(UUID creditCardId, UUID userId);
}
