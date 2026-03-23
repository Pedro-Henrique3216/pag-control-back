package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExpenseResponseDto(
        UUID id,
        @JsonProperty("invoice_number")
        String invoiceNumber,
        @JsonProperty("payment_type")
        PaymentType paymentType,
        @JsonProperty("supplier_id")
        UUID supplierId,
        LocalDate date,
        @JsonProperty("total_amount")
        BigDecimal totalAmount,
        @JsonProperty("category_id")
        UUID categoryId,
        List<InstallmentResponseDto> installments
) {
}
