package com.pedrohenrique.pagcontrolback.dtos.command;

import java.math.BigDecimal;

public record CreateCreditCardCommand(
        String name,
        int closingDay,
        int dueDay,
        BigDecimal creditLimit
) {
}
