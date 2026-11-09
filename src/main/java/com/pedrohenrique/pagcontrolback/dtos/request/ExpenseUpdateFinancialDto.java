package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateFinancialDto(
        @JsonProperty("expense_date")
        LocalDate expenseDate,
        @JsonProperty("total_amount")
        @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
        BigDecimal totalAmount
) {
}
