package com.pedrohenrique.pagcontrolback.dtos.command;

import com.pedrohenrique.pagcontrolback.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record CreateExpenseCommand(
        String invoiceNumber,
        PaymentType paymentType,
        UUID supplierId,
        LocalDate date,
        Map<Integer, String> barcodeByDueInDays,
        BigDecimal totalAmount,
        UUID categoryId,
        UUID userId
) {
}
