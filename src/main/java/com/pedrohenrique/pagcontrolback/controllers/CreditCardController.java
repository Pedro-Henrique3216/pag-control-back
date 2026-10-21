package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateCreditCardCommand;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateCreditCardCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.CreditCardRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.CreditCardUpdateDto;
import com.pedrohenrique.pagcontrolback.dtos.response.CreditCardResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.CreditCardMapper;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.usecases.CreateCreditCardUseCase;
import com.pedrohenrique.pagcontrolback.usecases.ListCreditCardUseCase;
import com.pedrohenrique.pagcontrolback.usecases.UpdateCreditCardUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/credit_cards")
public class CreditCardController {

    private final CreateCreditCardUseCase createCreditCardUseCase;
    private final ListCreditCardUseCase listCreditCardUseCase;
    private final UpdateCreditCardUseCase updateCreditCardUseCase;

    public CreditCardController(CreateCreditCardUseCase createCreditCardUseCase, ListCreditCardUseCase listCreditCardUseCase, UpdateCreditCardUseCase updateCreditCardUseCase) {
        this.createCreditCardUseCase = createCreditCardUseCase;
        this.listCreditCardUseCase = listCreditCardUseCase;
        this.updateCreditCardUseCase = updateCreditCardUseCase;
    }

    @PostMapping
    public ResponseEntity<CreditCardResponseDto> createCreditCard(
            @RequestBody CreditCardRequestDto request,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        CreateCreditCardCommand command = new CreateCreditCardCommand(
                request.name(),
                request.closingDay(),
                request.dueDay(),
                request.creditLimit()
        );

        CreditCard creditCard = createCreditCardUseCase.execute(command, userPrincipal.getId());

        URI uri = uriBuilder.path("/credit_cards/{id}").buildAndExpand(creditCard.getId()).toUri();

        return ResponseEntity.created(uri).body(CreditCardMapper.toCreditCardResponseDto(creditCard));
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponseDto>> findCreditCardByUserId(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<CreditCard> creditCards = listCreditCardUseCase.findCreditCardByUserId(userPrincipal.getId());

        List<CreditCardResponseDto> response = creditCards.stream()
                .map(CreditCardMapper::toCreditCardResponseDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditCardResponseDto> updateCreditCard(@RequestBody CreditCardUpdateDto dto, @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UpdateCreditCardCommand command = new UpdateCreditCardCommand(
                id,
                dto.name(),
                dto.closingDay(),
                dto.dueDay(),
                dto.creditLimit()
        );

        CreditCard creditCard = updateCreditCardUseCase.updateCreditCard(command, userPrincipal.getId());

        return ResponseEntity.ok(CreditCardMapper.toCreditCardResponseDto(creditCard));
    }
}
