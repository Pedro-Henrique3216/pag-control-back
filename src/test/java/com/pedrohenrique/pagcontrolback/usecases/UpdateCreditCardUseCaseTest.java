package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateCreditCardCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CreditCardNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.InvalidCreditCardNameException;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCreditCardUseCaseTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private UpdateCreditCardUseCase updateCreditCardUseCase;

    private UUID authenticatedUserId;
    private UUID creditCardId;
    private CreditCard creditCard;

    @BeforeEach
    void setUp() {
        authenticatedUserId = UUID.randomUUID();
        creditCardId = UUID.randomUUID();

        User user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "$2a$10$abcdefghijklmnopqrstuv",
                "11999999999",
                PersonType.PF
        );

        creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                new Money(new BigDecimal("2000.00")),
                user
        );
    }

    @Test
    void shouldUpdateCreditCardSuccessfully() {
        UpdateCreditCardCommand command = new UpdateCreditCardCommand(
                creditCardId,
                "Inter",
                15,
                25,
                new BigDecimal("5000.00")
        );

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId,
                authenticatedUserId
        )).thenReturn(creditCard);

        when(creditCardRepository.save(creditCard))
                .thenReturn(creditCard);

        CreditCard result = updateCreditCardUseCase.updateCreditCard(
                command,
                authenticatedUserId
        );

        assertNotNull(result);
        assertSame(creditCard, result);
        assertEquals("Inter", result.getName());
        assertEquals(15, result.getClosingDay());
        assertEquals(25, result.getDueDay());
        assertEquals(
                new BigDecimal("5000.00"),
                result.getCreditLimit().value()
        );

        verify(creditCardRepository).findCreditCardByCardIdAndUserId(
                creditCardId,
                authenticatedUserId
        );
        verify(creditCardRepository).save(creditCard);
    }

    @Test
    void shouldUpdateCreditCardPartially() {
        UpdateCreditCardCommand command = new UpdateCreditCardCommand(
                creditCardId,
                "Inter",
                null,
                null,
                null
        );

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId,
                authenticatedUserId
        )).thenReturn(creditCard);

        when(creditCardRepository.save(creditCard))
                .thenReturn(creditCard);

        CreditCard result = updateCreditCardUseCase.updateCreditCard(
                command,
                authenticatedUserId
        );

        assertEquals("Inter", result.getName());
        assertEquals(10, result.getClosingDay());
        assertEquals(17, result.getDueDay());
        assertEquals(
                new BigDecimal("2000.00"),
                result.getCreditLimit().value()
        );

        verify(creditCardRepository).save(creditCard);
    }

    @Test
    void shouldThrowWhenCreditCardDoesNotExist() {
        UpdateCreditCardCommand command = new UpdateCreditCardCommand(
                creditCardId,
                "Inter",
                15,
                25,
                new BigDecimal("5000.00")
        );

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId,
                authenticatedUserId
        )).thenReturn(null);

        CreditCardNotFoundException exception = assertThrows(
                CreditCardNotFoundException.class,
                () -> updateCreditCardUseCase.updateCreditCard(
                        command,
                        authenticatedUserId
                )
        );

        assertEquals("Credit card not found", exception.getMessage());

        verify(creditCardRepository, never()).save(any(CreditCard.class));
    }

    @Test
    void shouldThrowWhenCreditCardBelongsToAnotherUser() {
        UUID anotherUserId = UUID.randomUUID();

        UpdateCreditCardCommand command = new UpdateCreditCardCommand(
                creditCardId,
                "Inter",
                15,
                25,
                new BigDecimal("5000.00")
        );

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId,
                anotherUserId
        )).thenReturn(null);

        assertThrows(
                CreditCardNotFoundException.class,
                () -> updateCreditCardUseCase.updateCreditCard(
                        command,
                        anotherUserId
                )
        );

        verify(creditCardRepository, never()).save(any(CreditCard.class));
    }

    @Test
    void shouldNotSaveWhenEntityValidationFails() {
        UpdateCreditCardCommand command = new UpdateCreditCardCommand(
                creditCardId,
                "",
                null,
                null,
                null
        );

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId,
                authenticatedUserId
        )).thenReturn(creditCard);

        assertThrows(
                InvalidCreditCardNameException.class,
                () -> updateCreditCardUseCase.updateCreditCard(
                        command,
                        authenticatedUserId
                )
        );

        verify(creditCardRepository, never()).save(any(CreditCard.class));
    }
}