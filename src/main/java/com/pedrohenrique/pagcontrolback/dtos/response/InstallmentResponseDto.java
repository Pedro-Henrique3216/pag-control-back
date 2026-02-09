package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentResponseDto(
        UUID id,
        BigDecimal amount,
        @JsonProperty("due_date")
        LocalDate dueDate,
        String barcode,
        InstallmentStatus status
) {
}
