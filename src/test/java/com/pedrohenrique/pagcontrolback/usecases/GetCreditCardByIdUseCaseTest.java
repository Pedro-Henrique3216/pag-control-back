package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.CreditCardNotFoundException;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.repositories.CreditCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCreditCardByIdUseCaseTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private GetCreditCardByIdUseCase getCreditCardByIdUseCase;

    private UUID creditCardId;
    private UUID userId;
    private CreditCard creditCard;

    @BeforeEach
    void setUp() {
        creditCardId = UUID.randomUUID();
        userId = UUID.randomUUID();

        creditCard = mock(CreditCard.class);
    }

    @Test
    void shouldFindCreditCardByIdSuccessfully() {

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId,
                userId
        )).thenReturn(creditCard);

        CreditCard result = getCreditCardByIdUseCase
                .findCreditCardById(creditCardId, userId);

        assertNotNull(result);
        assertEquals(creditCard, result);

        verify(creditCardRepository).findCreditCardByCardIdAndUserId(
                creditCardId,
                userId
        );
    }

    @Test
    void shouldThrowExceptionWhenCreditCardIdIsNull() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getCreditCardByIdUseCase
                        .findCreditCardById(null, userId)
        );

        assertEquals(
                "credit card id cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(creditCardRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getCreditCardByIdUseCase
                        .findCreditCardById(creditCardId, null)
        );

        assertEquals(
                "user id cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(creditCardRepository);
    }

    @Test
    void shouldThrowExceptionWhenCreditCardDoesNotExist() {

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId,
                userId
        )).thenReturn(null);

        CreditCardNotFoundException exception = assertThrows(
                CreditCardNotFoundException.class,
                () -> getCreditCardByIdUseCase
                        .findCreditCardById(creditCardId, userId)
        );

        assertEquals(
                "credit card not found",
                exception.getMessage()
        );

        verify(creditCardRepository).findCreditCardByCardIdAndUserId(
                creditCardId,
                userId
        );
    }

    @Test
    void shouldNotFindCreditCardBelongingToAnotherUser() {

        when(creditCardRepository.findCreditCardByCardIdAndUserId(
                creditCardId,
                userId
        )).thenReturn(null);

        assertThrows(
                CreditCardNotFoundException.class,
                () -> getCreditCardByIdUseCase
                        .findCreditCardById(creditCardId, userId)
        );

        verify(creditCardRepository).findCreditCardByCardIdAndUserId(
                creditCardId,
                userId
        );
    }
}
