package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.model.PaymentType;
import com.pedrohenrique.pagcontrolback.model.RecurrenceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringExpenseResponseDto(
        UUID id,
        String description,

        @JsonProperty("payment_type")
        PaymentType paymentType,

        SupplierResponseDto supplier,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("total_amount")
        BigDecimal totalAmount,

        CategoryResponseDto category,

        @JsonProperty("credit_card")
        CreditCardSummaryResponseDto creditCard,

        @JsonProperty("recurrence_day")
        Integer recurrenceDay,

        @JsonProperty("recurrence_type")
        RecurrenceType recurrenceType,

        @JsonProperty("recurrence_interval")
        Integer recurrenceInterval,

        @JsonProperty("recurrence_end_date")
        LocalDate recurrenceEndDate
) {
}