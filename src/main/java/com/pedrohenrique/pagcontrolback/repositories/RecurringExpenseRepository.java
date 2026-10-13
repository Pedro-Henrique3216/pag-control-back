package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, UUID> {

    @Query(
            """
                SELECT re FROM RecurringExpense re
                WHERE re.active = true
                AND re.nextOccurrence <= :today
            """
    )
    List<RecurringExpense> findActiveRecurringExpensesDueOnOrBefore(LocalDate today);
}
