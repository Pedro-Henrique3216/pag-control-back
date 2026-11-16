package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateIncomeCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.IncomeRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.IncomeResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.IncomeMapper;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.usecases.CreateIncomeUseCase;
import com.pedrohenrique.pagcontrolback.usecases.ListIncomesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/incomes")
public class IncomeController {

    private final CreateIncomeUseCase createIncomeUseCase;
    private final ListIncomesUseCase listIncomesUseCase;

    public IncomeController(
            CreateIncomeUseCase createIncomeUseCase,
            ListIncomesUseCase listIncomesUseCase
    ) {
        this.createIncomeUseCase = createIncomeUseCase;
        this.listIncomesUseCase = listIncomesUseCase;
    }

    @PostMapping
    public ResponseEntity<IncomeResponseDto> createIncome(
            @RequestBody @Valid IncomeRequestDto dto,
            @AuthenticationPrincipal UserPrincipal user, UriComponentsBuilder uriBuilder
    ) {
        CreateIncomeCommand command = new CreateIncomeCommand(
                dto.amount(),
                dto.description(),
                dto.date(),
                user.getId(),
                dto.categoryId()
        );
        Income createdIncome = createIncomeUseCase.execute(command);
        URI uri = uriBuilder.path("/incomes/{id}").buildAndExpand(createdIncome.getId()).toUri();
        return ResponseEntity.created(uri).body(IncomeMapper.toDto(createdIncome));
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponseDto>> getAllIncomes(
            @RequestParam(required = false) LocalDate date,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        List<IncomeResponseDto> response = listIncomesUseCase
                .listIncomes(user.getId(), date)
                .stream()
                .map(IncomeMapper::toDto)
                .toList();

        return ResponseEntity.ok(response);
    }
}
