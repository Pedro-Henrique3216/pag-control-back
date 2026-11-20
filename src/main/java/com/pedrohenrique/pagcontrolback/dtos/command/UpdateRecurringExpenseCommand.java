package com.pedrohenrique.pagcontrolback.dtos.command;

import com.pedrohenrique.pagcontrolback.model.PaymentType;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateRecurringExpenseCommand(
        String description,
        BigDecimal amount,
        PaymentType paymentType,
        UUID supplierId,
        UUID categoryId,
        UUID creditCardId
) {
}
