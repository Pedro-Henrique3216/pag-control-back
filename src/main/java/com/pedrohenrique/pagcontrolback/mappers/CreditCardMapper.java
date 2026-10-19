package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.response.CreditCardResponseDto;
import com.pedrohenrique.pagcontrolback.dtos.response.CreditCardSummaryResponseDto;
import com.pedrohenrique.pagcontrolback.model.CreditCard;

public class CreditCardMapper {

    public static CreditCardSummaryResponseDto toCreditCardSummaryResponseDto(CreditCard creditCard) {
        return new CreditCardSummaryResponseDto(
            creditCard.getId(),
            creditCard.getName()
        );
    }

    public static CreditCardResponseDto toCreditCardResponseDto(CreditCard creditCard) {
        return new CreditCardResponseDto(
                creditCard.getId(),
                creditCard.getName(),
                creditCard.getClosingDay(),
                creditCard.getDueDay(),
                creditCard.getCreditLimit().value(),
                creditCard.isActive()
        );
    }
}
