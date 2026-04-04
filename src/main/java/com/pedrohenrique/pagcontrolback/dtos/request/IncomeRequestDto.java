package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IncomeRequestDto(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Total amount must be greater than zero")
        BigDecimal amount,
        @NotBlank(message = "Description is required")
        String description,
        @NotNull(message = "Date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonProperty("category_id")
        UUID categoryId
) {
}
