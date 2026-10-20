package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardResponseDto(
        UUID id,
        String name,
        @JsonProperty("closing_day")
        int closingDay,
        @JsonProperty("due_day")
        int dueDay,
        @JsonProperty("credit_limit")
        BigDecimal creditLimit,
        boolean active
) {
}
