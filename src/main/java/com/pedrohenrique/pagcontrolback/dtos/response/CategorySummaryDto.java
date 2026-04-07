package com.pedrohenrique.pagcontrolback.dtos.response;

import java.math.BigDecimal;

public record CategorySummaryDto(
        String description,
        BigDecimal total
) {
}
