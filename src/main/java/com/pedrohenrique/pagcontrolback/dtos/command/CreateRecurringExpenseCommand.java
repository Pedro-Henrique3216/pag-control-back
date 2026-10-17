package com.pedrohenrique.pagcontrolback.dtos.command;

import com.pedrohenrique.pagcontrolback.model.PaymentType;
import com.pedrohenrique.pagcontrolback.model.RecurrenceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRecurringExpenseCommand(
        String description,
        PaymentType paymentType,
        UUID supplierId,
        LocalDate startDate,
        UUID creditCardId,
        BigDecimal totalAmount,
        UUID categoryId,
        Integer recurrenceDay,
        RecurrenceType recurrenceType,
        Integer recurrenceInterval,
        LocalDate recurrenceEndDate
) {
}
