package com.pedrohenrique.pagcontrolback.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentReminderData(
        String description,
        BigDecimal amount,
        LocalDate dueDate,
        String barcode
) {
}
