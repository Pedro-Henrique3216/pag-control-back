package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record ExpenseRequestDto(
        @JsonProperty("invoice_number")
        String invoiceNumber,
        @NotBlank(message = "Description is required")
        String description,
        @NotNull(message = "Payment type is required")
        @JsonProperty("payment_type")
        PaymentType paymentType,
        @JsonProperty("supplier_id")
        UUID supplierId,
        @NotNull(message = "Date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonProperty("barcode_by_due_in_days")
        Map<Integer, String> barcodeByDueInDays,
        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Total amount must be greater than zero")
        @JsonProperty("total_amount")
        BigDecimal totalAmount,
        @JsonProperty(value = "category_id")
        UUID categoryId
) {
}

