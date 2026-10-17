package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import com.pedrohenrique.pagcontrolback.model.RecurrenceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringExpenseRequestDto(
        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Payment type is required")
        @JsonProperty("payment_type")
        PaymentType paymentType,

        @JsonProperty("supplier_id")
        UUID supplierId,

        @NotNull(message = "Start date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("credit_card_id")
        UUID creditCardId,

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
        @JsonProperty("total_amount")
        BigDecimal totalAmount,

        @JsonProperty("category_id")
        UUID categoryId,

        @Min(value = 1, message = "Recurrence day must be between 1 and 31")
        @Max(value = 31, message = "Recurrence day must be between 1 and 31")
        @JsonProperty("recurrence_day")
        Integer recurrenceDay,

        @NotNull(message = "Recurrence type is required")
        @JsonProperty("recurrence_type")
        RecurrenceType recurrenceType,

        @NotNull(message = "Recurrence interval is required")
        @Min(value = 1, message = "Recurrence interval must be greater than zero")
        @JsonProperty("recurrence_interval")
        Integer recurrenceInterval,

        @JsonProperty("recurrence_end_date")
        LocalDate recurrenceEndDate
) {
}