package com.pedrohenrique.pagcontrolback.dtos.request;

import java.time.YearMonth;
import java.util.UUID;

public record ListExpensesQuery(
        YearMonth month,
        UUID supplierId,
        String invoiceNumber
) {
}
