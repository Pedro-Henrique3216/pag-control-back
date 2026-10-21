package com.pedrohenrique.pagcontrolback.dtos.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateCreditCardCommand(
        UUID creditCardId,
        String name,
        Integer closingDay,
        Integer dueDay,
        BigDecimal creditLimit
) {
}
