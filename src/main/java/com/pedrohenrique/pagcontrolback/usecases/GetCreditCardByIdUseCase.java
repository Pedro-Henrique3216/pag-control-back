package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.CreditCardNotFoundException;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCreditCardByIdUseCase {

    private final CreditCardRepository creditCardRepository;

    public GetCreditCardByIdUseCase(
        CreditCardRepository creditCardRepository
    ) {
        this.creditCardRepository = creditCardRepository;
    }

    public CreditCard findCreditCardById(UUID creditCardId, UUID userId){
        if (creditCardId == null){
            throw new IllegalArgumentException("credit card id cannot be null");
        }

        if (userId == null){
            throw new IllegalArgumentException("user id cannot be null");
        }

        CreditCard creditCard = creditCardRepository.findCreditCardByCardIdAndUserId(creditCardId, userId);

        if (creditCard == null){
            throw new CreditCardNotFoundException("credit card not found");
        }

        return creditCard;
    }
}
