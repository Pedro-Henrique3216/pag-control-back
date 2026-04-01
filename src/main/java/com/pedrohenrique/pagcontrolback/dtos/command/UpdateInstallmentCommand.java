package com.pedrohenrique.pagcontrolback.dtos.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateInstallmentCommand(
        BigDecimal amount,
        LocalDate dueDate,
        String barcode,
        UUID userId,
        UUID installmentId
) {
}
