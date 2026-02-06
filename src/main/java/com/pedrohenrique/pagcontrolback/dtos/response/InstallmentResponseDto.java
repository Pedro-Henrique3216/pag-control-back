package com.pedrohenrique.pagcontrolback.dtos.response;

import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentResponseDto(
        UUID id,
        BigDecimal amount,
        LocalDate dueDate,
        String barcode,
        InstallmentStatus status
) {
}
