package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.request.ListExpensesQuery;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.exceptions.FutureMonthNotAllowedException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.mappers.ExpenseMapper;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepositoryCustom;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class ListExpensesUseCase {

    private final ExpenseRepositoryCustom expenseRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;

    public ListExpensesUseCase(ExpenseRepositoryCustom expenseRepository, UserRepository userRepository, SupplierRepository supplierRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<Expense> execute(ListExpensesQuery query, UUID userId) {
        if (userId == null) {
            throw new UserIdRequiredException("User id is required.");
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found.");
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
