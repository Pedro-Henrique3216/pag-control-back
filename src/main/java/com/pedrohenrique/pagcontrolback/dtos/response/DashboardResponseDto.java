package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponseDto(
        @JsonProperty("total_income")
        BigDecimal totalIncome,
        @JsonProperty("total_expense")
        BigDecimal totalExpense,
        BigDecimal balance,
        @JsonProperty("expenses_by_category")
        List<CategorySummaryDto> expensesByCategory
) {
}
