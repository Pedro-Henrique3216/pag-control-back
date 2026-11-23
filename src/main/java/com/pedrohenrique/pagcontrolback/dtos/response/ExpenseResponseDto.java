package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.dtos.helper.CategoryDto;
import com.pedrohenrique.pagcontrolback.dtos.helper.SupplierDto;
import com.pedrohenrique.pagcontrolback.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExpenseResponseDto(
        UUID id,
        String description,
        @JsonProperty("invoice_number")
        String invoiceNumber,
        @JsonProperty("payment_type")
        PaymentType paymentType,
        @JsonProperty("total_amount")
        BigDecimal totalAmount,
        LocalDate date,
        SupplierDto suppler,
        CategoryDto category,
        @JsonProperty("number_of_installments")
        int numberOfInstallments
) {
}
