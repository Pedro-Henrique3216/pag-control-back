package com.pedrohenrique.pagcontrolback.factories;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.response.CreditCardResponseDto;
import com.pedrohenrique.pagcontrolback.model.CreditCard;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditCardResponseFactoryTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private CreditCardResponseFactory creditCardResponseFactory;

    private CreditCard creditCard;

    @BeforeEach
    void setUp() {
        creditCard = new CreditCard(
                "teste",
                9,
                15,
                Money.of(new BigDecimal("5000.00")),
                new User()
        );
    }

    @Test
    void shouldCreateCreditCardResponseSuccessfully() {

        BigDecimal usedLimit = new BigDecimal("1200.00");

        when(expenseRepository.findUsedLimitByCreditCardId(
                any(),
                any()
        )).thenReturn(usedLimit);

        CreditCardResponseDto response =
                creditCardResponseFactory.create(creditCard);

        assertNotNull(response);

        assertEquals(
                new BigDecimal("5000.00"),
                response.creditLimit()
        );

        assertEquals(
                new BigDecimal("1200.00"),
                response.usedLimit()
        );

        assertEquals(
                new BigDecimal("3800.00"),
                response.availableLimit()
        );

        verify(expenseRepository).findUsedLimitByCreditCardId(
                any(),
                any()
        );
    }

    @Test
    void shouldReturnZeroUsedLimitWhenCardHasNoExpenses() {

        when(expenseRepository.findUsedLimitByCreditCardId(
                any(),
                any()
        )).thenReturn(BigDecimal.ZERO);

        CreditCardResponseDto response =
                creditCardResponseFactory.create(creditCard);

        assertEquals(
                new BigDecimal("5000.00"),
                response.availableLimit()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.usedLimit()
        );
    }

    @Test
    void shouldReturnZeroAvailableLimitWhenCreditLimitIsFullyUsed() {

        when(expenseRepository.findUsedLimitByCreditCardId(
                any(),
                any()
        )).thenReturn(new BigDecimal("5000.00"));

        CreditCardResponseDto response =
                creditCardResponseFactory.create(creditCard);

        assertEquals(
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                response.availableLimit()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                response.usedLimit()
        );
    }
}
