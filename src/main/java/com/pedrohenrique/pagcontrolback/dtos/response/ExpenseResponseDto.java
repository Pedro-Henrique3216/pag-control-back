package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.dtos.helper.CategoryDto;
import com.pedrohenrique.pagcontrolback.dtos.helper.SupplierDto;
import com.pedrohenrique.pagcontrolback.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        SupplierDto supplier,
        CategoryDto category,
        @JsonProperty("number_of_installments_paid")
        int numberOfInstallmentsPaid,
        @JsonProperty("number_of_installments")
        int numberOfInstallments
) {
}
