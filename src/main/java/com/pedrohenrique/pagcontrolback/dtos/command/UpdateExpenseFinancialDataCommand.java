package com.pedrohenrique.pagcontrolback.dtos.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateExpenseFinancialDataCommand(
        LocalDate expenseDate,
        BigDecimal totalAmount
) {
}
