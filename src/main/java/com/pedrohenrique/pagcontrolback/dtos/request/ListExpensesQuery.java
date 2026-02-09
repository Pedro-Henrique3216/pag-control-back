package com.pedrohenrique.pagcontrolback.dtos.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;
import java.util.UUID;

public record ListExpensesQuery(
        YearMonth month,
        UUID supplierId,
        String invoiceNumber
) {
}
