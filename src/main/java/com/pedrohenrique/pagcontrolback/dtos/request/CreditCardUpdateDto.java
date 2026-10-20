package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CreditCardUpdateDto(
        String name,
        @JsonProperty("closing_day")
        int closingDay,
        @JsonProperty("due_day")
        int dueDay,
        @JsonProperty("credit_limit")
        BigDecimal creditLimit
) {
}
