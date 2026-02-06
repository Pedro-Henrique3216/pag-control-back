package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.ExpenseMapper;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.usecases.CreateExpenseWithInstallmentsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final CreateExpenseWithInstallmentsUseCase createExpenseWithInstallmentsUseCase;

    public ExpenseController(CreateExpenseWithInstallmentsUseCase createExpenseWithInstallmentsUseCase) {
        this.createExpenseWithInstallmentsUseCase = createExpenseWithInstallmentsUseCase;
    }

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ExpenseResponseDto> createExpenseWithInstallments(@Valid @RequestBody ExpenseRequestDto expenseRequestDto, UriComponentsBuilder uriBuilder, @PathVariable UUID userId) {

        Expense expense = ExpenseMapper.toDomain(expenseRequestDto);

        var expenseSaved = createExpenseWithInstallmentsUseCase.execute(
                userId,
                expenseRequestDto.supplierId(),
                expense,
                expenseRequestDto.barcodeByDueInDays(),
                expenseRequestDto.totalAmount()
        );

        URI uri = uriBuilder.path("/expenses/{id}").buildAndExpand(expenseSaved.getId()).toUri();
        return ResponseEntity.created(uri).body(ExpenseMapper.fromDomain(expenseSaved));

    }
}
