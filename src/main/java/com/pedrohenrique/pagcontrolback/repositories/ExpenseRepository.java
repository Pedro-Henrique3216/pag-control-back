package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("""
            SELECT e FROM Expense e
            WHERE e.recurring = true
            AND e.active = true
    """)
    List<Expense> findAllActiveRecurringExpenses();

    @Query(
            """
                SELECT EXISTS
                            (SELECT 1 FROM Expense e
                                        WHERE e.recurringExpense.id = :recurringExpenseId
                                                    AND e.expenseDate = :date)
            """
    )
    boolean existsByRecurringExpenseIdAndOccurrenceDate(UUID recurringExpenseId, LocalDate date);
}
