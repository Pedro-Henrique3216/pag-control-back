package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateRecurringExpenseCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.RecurringExpenseRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.RecurringExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.RecurringExpenseMapper;
import com.pedrohenrique.pagcontrolback.model.RecurringExpense;
import com.pedrohenrique.pagcontrolback.usecases.CreateRecurringExpenseUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/recurring-expenses")
public class RecurringExpenseController {

    private final CreateRecurringExpenseUseCase createRecurringExpenseUseCase;

    public RecurringExpenseController(
            CreateRecurringExpenseUseCase createRecurringExpenseUseCase
    ) {
        this.createRecurringExpenseUseCase = createRecurringExpenseUseCase;
    }


    @PostMapping
    public ResponseEntity<RecurringExpenseResponseDto> createRecurringExpense(
            @RequestBody @Valid RecurringExpenseRequestDto request,
            UriComponentsBuilder uriComponentsBuilder,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        CreateRecurringExpenseCommand command = new CreateRecurringExpenseCommand(
                request.description(),
                request.paymentType(),
                request.supplierId(),
                request.startDate(),
                request.creditCardId(),
                request.totalAmount(),
                request.categoryId(),
                request.recurrenceDay(),
                request.recurrenceType(),
                request.recurrenceInterval(),
                request.recurrenceEndDate()
        );

        RecurringExpense recurringExpense = createRecurringExpenseUseCase.execute(command, userPrincipal.getId());
        URI uri = uriComponentsBuilder.path("/recurring-expenses/{id}").buildAndExpand(recurringExpense.getId()).toUri();
        return ResponseEntity.created(uri).body(RecurringExpenseMapper.fromDomain(recurringExpense));
    }
}


