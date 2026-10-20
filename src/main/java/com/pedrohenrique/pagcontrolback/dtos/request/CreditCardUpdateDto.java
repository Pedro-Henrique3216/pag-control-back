package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CreditCardUpdateDto(
        String name,
        @JsonProperty("closing_day")
        Integer closingDay,
        @JsonProperty("due_day")
        Integer dueDay,
        @JsonProperty("credit_limit")
        BigDecimal creditLimit
) {
}
