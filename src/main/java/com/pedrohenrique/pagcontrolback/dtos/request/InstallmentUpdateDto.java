package com.pedrohenrique.pagcontrolback.dtos.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentUpdateDto(
        @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
        BigDecimal amount,
        LocalDate dueDate,
        String barcode
) {
}
