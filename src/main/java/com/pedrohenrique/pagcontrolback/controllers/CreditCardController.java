package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateCreditCardCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.CreditCardRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.CreditCardResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.CreditCardMapper;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.usecases.CreateCreditCardUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/credit_cards")
public class CreditCardController {

    private final CreateCreditCardUseCase createCreditCardUseCase;

    public CreditCardController(CreateCreditCardUseCase createCreditCardUseCase) {
        this.createCreditCardUseCase = createCreditCardUseCase;
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
}
