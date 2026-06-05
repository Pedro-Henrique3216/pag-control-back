package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.InvalidAmountException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Nested
    class Success {

        @Test
        void shouldCreateMoneyWhenValidAmountIsProvided() {
            Money money = new Money(BigDecimal.valueOf(100));

            assertEquals(BigDecimal.valueOf(100.00).doubleValue(), money.value().doubleValue());
        }

        @Test
        void shouldRoundValueToTwoDecimalPlaces() {
            Money money = new Money(BigDecimal.valueOf(10.555));

            assertEquals(BigDecimal.valueOf(10.56), money.value());
        }

        @Test
        void shouldAddMoneyCorrectly() {
            Money first = new Money(BigDecimal.valueOf(50));
            Money second = new Money(BigDecimal.valueOf(25));

            Money result = first.add(second);

            assertEquals(BigDecimal.valueOf(75.00).doubleValue(), result.value().doubleValue());
        }

        @Test
        void shouldSubtractMoneyCorrectly() {
            Money first = new Money(BigDecimal.valueOf(100));
            Money second = new Money(BigDecimal.valueOf(40));

            Money result = first.subtract(second);

            assertEquals(BigDecimal.valueOf(60.00).doubleValue(), result.value().doubleValue());
        }

        @Test
        void shouldMultiplyMoneyCorrectly() {
            Money money = new Money(BigDecimal.valueOf(20));

            Money result = money.multiply(3);

            assertEquals(BigDecimal.valueOf(60.00).doubleValue(), result.value().doubleValue());
        }

        @Test
        void shouldDivideMoneyCorrectly() {
            Money money = new Money(BigDecimal.valueOf(100));

            Money result = money.divide(3);

            assertEquals(BigDecimal.valueOf(33.33), result.value());
        }

        @Test
        void shouldReturnZeroConstantCorrectly() {
            assertEquals(BigDecimal.ZERO.setScale(2), Money.ZERO.value());
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldThrowExceptionWhenAmountIsNull() {
            assertThrows(
                    InvalidAmountException.class,
                    () -> new Money(null)
            );
        }

        @Test
        void shouldThrowExceptionWhenAmountIsNegative() {
            assertThrows(
                    InvalidAmountException.class,
                    () -> new Money(BigDecimal.valueOf(-10))
            );
        }
    }
}