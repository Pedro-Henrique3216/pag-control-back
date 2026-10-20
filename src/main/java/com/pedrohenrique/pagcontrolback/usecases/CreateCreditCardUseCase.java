package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateCreditCardCommand;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateCreditCardUseCase {

    private final CreditCardRepository creditCardRepository;
    private final UserRepository userRepository;

    public CreateCreditCardUseCase(CreditCardRepository creditCardRepository, UserRepository userRepository) {
        this.creditCardRepository = creditCardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CreditCard execute(CreateCreditCardCommand command, UUID authenticatedUserId) {
        User user = userRepository.getReferenceById(authenticatedUserId);

        return creditCardRepository.save(
                new CreditCard(
                        command.name(),
                        command.closingDay(),
                        command.dueDay(),
                        Money.of(command.creditLimit()),
                        user
                )
        );
    }
}
