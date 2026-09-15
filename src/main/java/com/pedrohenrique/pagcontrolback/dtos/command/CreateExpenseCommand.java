package com.pedrohenrique.pagcontrolback.dtos.command;

import com.pedrohenrique.pagcontrolback.model.PaymentType;
import com.pedrohenrique.pagcontrolback.model.RecurrenceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record CreateExpenseCommand(
        String invoiceNumber,
        String description,
        PaymentType paymentType,
        UUID supplierId,
        LocalDate date,
        Map<Integer, String> barcodeByDueInDays,
        BigDecimal totalAmount,
        UUID categoryId,
        boolean isRecurring,
        RecurrenceType recurrenceType,
        Integer recurrenceInterval,
        LocalDate recurrenceEndDate
) {
}
