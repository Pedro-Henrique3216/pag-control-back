package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record ExpenseRequestDto(
        String invoiceNumber,
        @NotNull(message = "Payment type is required")
        PaymentType paymentType,
        @NotNull(message = "Supplier ID is required")
        UUID supplierId,
        @NotNull(message = "Date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        Map<Integer, String> barcodeByDueInDays,
        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Total amount must be greater than zero")
        BigDecimal totalAmount
) {
}

