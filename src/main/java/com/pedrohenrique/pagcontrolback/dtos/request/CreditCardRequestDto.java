package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreditCardRequestDto(
        @NotBlank(message = "name is required")
        String name,
        @JsonProperty("closing_day")
        @Min(value = 1, message = "closing day must be between 1 and 31")
        @Max(value = 31, message = "closing day must be between 1 and 31")
        int closingDay,
        @JsonProperty("due_day")
        @Min(value = 1, message = "due day day must be between 1 and 31")
        @Max(value = 31, message = "due day day must be between 1 and 31")
        int dueDay,
        @JsonProperty("credit_limit")
        @NotNull(message = "credit limit is required")
        @DecimalMin(value = "0.01", message = "credit limit must be greater than zero")
        BigDecimal creditLimit
) {
}
