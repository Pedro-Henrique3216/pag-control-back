package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IncomeUpdateDto(

        @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places.")
        BigDecimal amount,

        @Size(min = 1, max = 100, message = "Description must contain between 1 and 100 characters.")
        String description,

        LocalDate date,

        @JsonProperty("category_id")
        UUID categoryId

) {
}