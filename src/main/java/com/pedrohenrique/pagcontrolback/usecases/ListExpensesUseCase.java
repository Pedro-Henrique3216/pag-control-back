package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.request.ListExpensesQuery;
import com.pedrohenrique.pagcontrolback.exceptions.FutureMonthNotAllowedException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepositoryCustom;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class ListExpensesUseCase {

    private final ExpenseRepositoryCustom expenseRepository;
    private final SupplierRepository supplierRepository;

    public ListExpensesUseCase(ExpenseRepositoryCustom expenseRepository, SupplierRepository supplierRepository) {
        this.expenseRepository = expenseRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<Expense> execute(ListExpensesQuery query, UUID userId) {
        if (userId == null) {
            throw new UserIdRequiredException("User id is required.");
        }

        if (query.supplierId() != null &&
                !supplierRepository.existsById(query.supplierId())) {
            throw new SupplierNotFoundException("Supplier not found.");
        }

        if (query.month() != null && query.month().isAfter(YearMonth.now())) {
            throw new FutureMonthNotAllowedException("Month cannot be in the future.");
        }

        return expenseRepository.search(query, userId);
    }

}
