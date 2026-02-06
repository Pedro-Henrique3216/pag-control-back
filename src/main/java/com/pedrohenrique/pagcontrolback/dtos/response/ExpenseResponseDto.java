package com.pedrohenrique.pagcontrolback.dtos.response;

import com.pedrohenrique.pagcontrolback.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExpenseResponseDto(
        UUID id,
        String invoiceNumber,
        PaymentType paymentType,
        UUID supplierId,
        LocalDate date,
        BigDecimal totalAmount,
        List<InstallmentResponseDto> installments
) {
}
