package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateIncomeCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.IncomeRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.IncomeResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.IncomeMapper;
import com.pedrohenrique.pagcontrolback.model.Income;
import com.pedrohenrique.pagcontrolback.usecases.CreateIncomeUseCase;
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
@RequestMapping("/incomes")
public class IncomeController {

    private final CreateIncomeUseCase createIncomeUseCase;

    public IncomeController(CreateIncomeUseCase createIncomeUseCase) {
        this.createIncomeUseCase = createIncomeUseCase;
    }

    @PostMapping
    public ResponseEntity<IncomeResponseDto> createIncome(@RequestBody @Valid IncomeRequestDto dto, @AuthenticationPrincipal UserPrincipal user, UriComponentsBuilder uriBuilder) {
        CreateIncomeCommand command = new CreateIncomeCommand(
                dto.amount(),
                dto.description(),
                dto.date(),
                dto.categoryId(),
                user.getId()
        );
        Income createdIncome = createIncomeUseCase.execute(command);
        URI uri = uriBuilder.path("/incomes/{id}").buildAndExpand(createdIncome.getId()).toUri();
        return ResponseEntity.created(uri).body(IncomeMapper.toDto(createdIncome));
    }
}
