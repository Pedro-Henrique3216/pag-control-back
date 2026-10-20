
package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListCreditCardUseCaseTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private ListCreditCardUseCase listCreditCardUseCase;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "$2a$10$abcdefghijklmnopqrstuv",
                "11999999999",
                PersonType.PF
        );
    }

    @Test
    void shouldReturnCreditCardsByUserId() {
        CreditCard firstCard = new CreditCard(
                "Nubank",
                10,
                17,
                Money.of(new BigDecimal("5000.00")),
                user
        );

        CreditCard secondCard = new CreditCard(
                "Inter",
                5,
                12,
                Money.of(new BigDecimal("3000.00")),
                user
        );

        when(creditCardRepository.findCreditCardByUserId(userId))
                .thenReturn(List.of(firstCard, secondCard));

        List<CreditCard> result =
                listCreditCardUseCase.findCreditCardByUserId(userId);

        assertEquals(2, result.size());
        assertEquals("Nubank", result.get(0).getName());
        assertEquals("Inter", result.get(1).getName());

        verify(creditCardRepository).findCreditCardByUserId(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoCreditCards() {
        when(creditCardRepository.findCreditCardByUserId(userId))
                .thenReturn(List.of());

        List<CreditCard> result =
                listCreditCardUseCase.findCreditCardByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(creditCardRepository).findCreditCardByUserId(userId);
    }

    @Test
    void shouldReturnOnlyCardsReturnedForAuthenticatedUser() {
        CreditCard creditCard = new CreditCard(
                "Nubank",
                10,
                17,
                Money.of(new BigDecimal("5000.00")),
                user
        );

        when(creditCardRepository.findCreditCardByUserId(userId))
                .thenReturn(List.of(creditCard));

        List<CreditCard> result =
                listCreditCardUseCase.findCreditCardByUserId(userId);

        assertEquals(1, result.size());
        assertSame(creditCard, result.get(0));

        verify(creditCardRepository).findCreditCardByUserId(userId);
        verifyNoMoreInteractions(creditCardRepository);
    }
}
