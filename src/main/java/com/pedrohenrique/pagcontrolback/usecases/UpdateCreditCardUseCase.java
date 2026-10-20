package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateCreditCardCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CreditCardNotFoundException;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateCreditCardUseCase {

    private final CreditCardRepository creditCardRepository;

    public UpdateCreditCardUseCase(CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    public CreditCard updateCreditCard(UpdateCreditCardCommand command, UUID authenticatedUserId) {
        CreditCard creditCard = creditCardRepository.findCreditCardByCardIdAndUserId(command.creditCardId(), authenticatedUserId);
        if (creditCard == null) {
            throw new CreditCardNotFoundException("Credit card not found");
        }

        creditCard.updateCreditCard(
                command.name(),
                command.closingDay(),
                command.dueDay(),
                command.creditLimit()
        );

        return creditCardRepository.save(creditCard);
    }
}
