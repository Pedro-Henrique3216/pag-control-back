package com.pedrohenrique.pagcontrolback.ValueObjects;

import com.pedrohenrique.pagcontrolback.exceptions.InvalidAmountException;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public record Money(BigDecimal value) {

    public static Money ZERO = new Money(BigDecimal.ZERO);

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public Money {
        if (value == null) {
            throw new InvalidAmountException("Amount cannot be null");
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Amount cannot be negative");
        }

        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public Money subtract(Money other) {
        return new Money(value.subtract(other.value));
    }

    public Money divide(int divisor) {
        return new Money(value.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.DOWN));
    }

    public Money multiply(int factor) {
        return new Money(value.multiply(BigDecimal.valueOf(factor)));
    }

    public Money add(Money other) {
        return new Money(value.add(other.value));
    }
}
