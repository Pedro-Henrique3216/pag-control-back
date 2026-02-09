package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.request.ListExpensesQuery;
import com.pedrohenrique.pagcontrolback.model.Expense;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepositoryCustom {
    List<Expense> search(ListExpensesQuery query, UUID userId);
}
