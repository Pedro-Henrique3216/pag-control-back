package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("""
            SELECT e FROM Expense e
            WHERE e.recurring = true
            AND e.active = true
    """)
    List<Expense> findAllActiveRecurringExpenses();
}
