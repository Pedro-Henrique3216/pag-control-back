package com.pedrohenrique.pagcontrolback.dtos.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateIncomeCommand(
        BigDecimal amount,
        String description,
        LocalDate date,
        UUID userId,
        UUID categoryId

) {
}
