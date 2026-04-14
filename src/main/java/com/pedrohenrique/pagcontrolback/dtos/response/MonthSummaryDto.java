package com.pedrohenrique.pagcontrolback.dtos.response;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthSummaryDto(
        YearMonth month,
        BigDecimal income,
        BigDecimal expense
) {
}
