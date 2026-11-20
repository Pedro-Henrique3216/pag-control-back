package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RecurringExpenseUpdateDto(
        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
        @JsonProperty("total_amount")
        BigDecimal totalAmount,

        @NotNull(message = "Payment type is required")
        @JsonProperty("payment_type")
        PaymentType paymentType,

        @JsonProperty("supplier_id")
        UUID supplierId,

        @JsonProperty("credit_card_id")
        UUID creditCardId,

        @JsonProperty("category_id")
        UUID categoryId
) {
}
