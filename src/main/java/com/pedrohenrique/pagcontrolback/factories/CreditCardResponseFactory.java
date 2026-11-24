package com.pedrohenrique.pagcontrolback.factories;

import com.pedrohenrique.pagcontrolback.dtos.response.CreditCardResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.CreditCardMapper;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreditCardResponseFactory {

    private final ExpenseRepository expenseRepository;

    public CreditCardResponseFactory(
            ExpenseRepository expenseRepository
    ) {
        this.expenseRepository = expenseRepository;
    }

    public CreditCardResponseDto create(CreditCard creditCard) {
        BigDecimal creditLimit = creditCard.getCreditLimit().value();

        BigDecimal usedLimit = expenseRepository
                .findUsedLimitByCreditCardId(creditCard.getId(), creditCard.getUser().getId());

        BigDecimal availableLimit = creditLimit.subtract(usedLimit);

        return CreditCardMapper.toCreditCardResponseDto(
                creditCard,
                usedLimit,
                availableLimit
        );
    }
}