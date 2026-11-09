package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateExpenseCommand;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateExpenseCommand;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateExpenseFinancialDataCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseUpdateDto;
import com.pedrohenrique.pagcontrolback.dtos.request.ExpenseUpdateFinancialDto;
import com.pedrohenrique.pagcontrolback.dtos.request.ListExpensesQuery;
import com.pedrohenrique.pagcontrolback.dtos.response.ExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.ExpenseMapper;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.usecases.*;
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
    private final GetExpenseByIdUseCase getExpenseByIdUseCase;
    private final UpdateExpenseUseCase updateExpenseUseCase;
    private final UpdateExpenseFinancialDataUseCase updateExpenseFinancialDataUseCase;
    private final DeleteExpenseUseCase deleteExpenseUseCase;

    public ExpenseController(
            CreateExpenseWithInstallmentsUseCase createExpenseWithInstallmentsUseCase,
            ListExpensesUseCase listExpensesUseCase,
            GetExpenseByIdUseCase getExpenseByIdUseCase,
            UpdateExpenseUseCase updateExpenseUseCase,
            UpdateExpenseFinancialDataUseCase updateExpenseFinancialDataUseCase,
            DeleteExpenseUseCase deleteExpenseUseCase
    ) {
        this.createExpenseWithInstallmentsUseCase = createExpenseWithInstallmentsUseCase;
        this.listExpensesUseCase = listExpensesUseCase;
        this.getExpenseByIdUseCase = getExpenseByIdUseCase;
        this.updateExpenseUseCase = updateExpenseUseCase;
        this.updateExpenseFinancialDataUseCase = updateExpenseFinancialDataUseCase;
        this.deleteExpenseUseCase = deleteExpenseUseCase;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDto> createExpenseWithInstallments(
            @Valid @RequestBody ExpenseRequestDto expenseRequestDto,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        CreateExpenseCommand command = new CreateExpenseCommand(
                expenseRequestDto.invoiceNumber(),
                expenseRequestDto.description(),
                expenseRequestDto.paymentType(),
                expenseRequestDto.supplierId(),
                expenseRequestDto.date(),
                expenseRequestDto.barcodeByDueInDays(),
                expenseRequestDto.numberOfInstallments(),
                expenseRequestDto.creditCardId(),
                expenseRequestDto.totalAmount(),
                expenseRequestDto.categoryId()
        );

        var expenseSaved = createExpenseWithInstallmentsUseCase.execute(
                user.getId(),
                command
        );

        URI uri = uriBuilder.path("/expenses/{id}").buildAndExpand(expenseSaved.getId()).toUri();
        return ResponseEntity.created(uri).body(ExpenseMapper.fromDomain(expenseSaved));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ExpenseResponseDto>> getExpenses(
            @AuthenticationPrincipal UserPrincipal user,

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

    @GetMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ExpenseResponseDto> getExpense(
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Expense expense = getExpenseByIdUseCase.getExpenseById(expenseId, user.getId());
        return ResponseEntity.ok(ExpenseMapper.fromDomain(expense));
    }

    @PutMapping("/{expenseId}/details")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody ExpenseUpdateDto dto
    ) {
        UpdateExpenseCommand command = new UpdateExpenseCommand(
                dto.description(),
                dto.invoiceNumber(),
                dto.supplierId(),
                dto.categoryId()
        );

        Expense expense = updateExpenseUseCase.updateExpense(expenseId, user.getId(), command);
        return ResponseEntity.ok(ExpenseMapper.fromDomain(expense));
    }

    @PutMapping("/{expenseId}/financial")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ExpenseResponseDto> updateExpenseFinancial(
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ExpenseUpdateFinancialDto dto
    ){
        UpdateExpenseFinancialDataCommand command = new UpdateExpenseFinancialDataCommand(
                dto.expenseDate(),
                dto.totalAmount()
        );

        Expense expense = updateExpenseFinancialDataUseCase.updateFinancialData(
                expenseId, user.getId(), command
        );
        return ResponseEntity.ok(ExpenseMapper.fromDomain(expense));
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        deleteExpenseUseCase.deleteExpense(
                expenseId,
                userPrincipal.getId()
        );
    }

}
