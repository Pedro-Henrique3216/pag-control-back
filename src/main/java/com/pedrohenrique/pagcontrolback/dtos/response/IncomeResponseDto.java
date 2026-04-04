package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IncomeResponseDto(
        UUID id,
        BigDecimal amount,
        String description,
        LocalDate date,
        @JsonProperty("category_id")
        UUID categoryId
) {
}
