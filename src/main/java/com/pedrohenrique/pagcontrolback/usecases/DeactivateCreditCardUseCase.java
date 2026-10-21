package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.CreditCardNotFoundException;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeactivateCreditCardUseCase {

    private final CreditCardRepository creditCardRepository;

    public DeactivateCreditCardUseCase(CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    @Transactional
    public void deactivateCreditCard(UUID creditCardId, UUID authenticatedUserId) {
        CreditCard creditCard = creditCardRepository.findCreditCardByCardIdAndUserId(creditCardId, authenticatedUserId);
        if (creditCard ==  null) {
            throw new CreditCardNotFoundException("Credit card not found");
        }

        if (!creditCard.isActive()) {
            return;
        }

        creditCard.deactivateCreditCard();
        creditCardRepository.save(creditCard);
    }
}
