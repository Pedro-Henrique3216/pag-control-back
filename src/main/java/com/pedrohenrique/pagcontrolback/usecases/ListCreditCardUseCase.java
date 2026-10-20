package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListCreditCardUseCase {

    private final CreditCardRepository creditCardRepository;

    public ListCreditCardUseCase(CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    public List<CreditCard> findCreditCardByUserId(UUID authenticatedUserId) {
        return creditCardRepository.findCreditCardByUserId(authenticatedUserId);
    }
}
