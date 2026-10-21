package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.CreditCardNotFoundException;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeactivateCreditCardUseCaseTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private DeactivateCreditCardUseCase deactivateCreditCardUseCase;

    private UUID creditCardId;
    private UUID authenticatedUserId;
    private CreditCard creditCard;

    @BeforeEach
    void setUp() {
        creditCardId = UUID.randomUUID();
        authenticatedUserId = UUID.randomUUID();
        creditCard = mock(CreditCard.class);
    }

    @Test
    @DisplayName("Should deactivate credit card successfully")
    void shouldDeactivateCreditCardSuccessfully() {
        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId, authenticatedUserId
        )).thenReturn(creditCard);

        when(creditCard.isActive()).thenReturn(true);

        deactivateCreditCardUseCase.deactivateCreditCard(
                creditCardId, authenticatedUserId
        );

        verify(creditCard).deactivateCreditCard();
        verify(creditCardRepository).save(creditCard);
    }

    @Test
    @DisplayName("Should throw exception when credit card does not exist or does not belong to user")
    void shouldThrowExceptionWhenCreditCardDoesNotExist() {
        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId, authenticatedUserId
        )).thenReturn(null);

        assertThrows(
                CreditCardNotFoundException.class,
                () -> deactivateCreditCardUseCase.deactivateCreditCard(
                        creditCardId, authenticatedUserId
                )
        );

        verify(creditCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not save credit card when it is already inactive")
    void shouldNotSaveCreditCardWhenAlreadyInactive() {
        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId, authenticatedUserId
        )).thenReturn(creditCard);

        when(creditCard.isActive()).thenReturn(false);

        deactivateCreditCardUseCase.deactivateCreditCard(
                creditCardId, authenticatedUserId
        );

        verify(creditCard, never()).deactivateCreditCard();
        verify(creditCardRepository, never()).save(any());
    }
}