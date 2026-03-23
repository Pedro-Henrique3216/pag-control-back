package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.ListExpensesQuery;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.ExpenseMapper;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.usecases.CreateExpenseWithInstallmentsUseCase;
import com.pedrohenrique.pagcontrolback.usecases.ListExpensesUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final CreateExpenseWithInstallmentsUseCase createExpenseWithInstallmentsUseCase;
    private final ListExpensesUseCase listExpensesUseCase;

    public ExpenseController(CreateExpenseWithInstallmentsUseCase createExpenseWithInstallmentsUseCase, ListExpensesUseCase listExpensesUseCase) {
        this.createExpenseWithInstallmentsUseCase = createExpenseWithInstallmentsUseCase;
        this.listExpensesUseCase = listExpensesUseCase;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ExpenseResponseDto> createExpenseWithInstallments(
            @Valid @RequestBody ExpenseRequestDto expenseRequestDto,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal User user
    ) {

        Expense expense = ExpenseMapper.toDomain(expenseRequestDto);

        var expenseSaved = createExpenseWithInstallmentsUseCase.execute(
                user.getId(),
                expenseRequestDto.supplierId(),
                expenseRequestDto.categoryId(),
                expense,
                expenseRequestDto.barcodeByDueInDays(),
                expenseRequestDto.totalAmount()
        );

        URI uri = uriBuilder.path("/expenses/{id}").buildAndExpand(expenseSaved.getId()).toUri();
        return ResponseEntity.created(uri).body(ExpenseMapper.fromDomain(expenseSaved));
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ExpenseResponseDto>> getExpenses(
            @AuthenticationPrincipal User user,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month,

            @RequestParam(required = false, name = "supplier_id")
            UUID supplierId,

            @RequestParam(required = false, name = "invoice_number")
            String invoiceNumber
    ) {

        ListExpensesQuery query = new ListExpensesQuery(
                month,
                supplierId,
                invoiceNumber
        );
        List<Expense> expenses = listExpensesUseCase.execute(
                query,
                user.getId()
        );

        return ResponseEntity.ok(expenses.stream()
                .map(ExpenseMapper::fromDomain)
                .toList());
    }

}
