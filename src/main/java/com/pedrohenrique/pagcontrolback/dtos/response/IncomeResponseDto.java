package com.pedrohenrique.pagcontrolback.dtos.response;

import com.pedrohenrique.pagcontrolback.dtos.helper.CategoryDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IncomeResponseDto(
        UUID id,
        BigDecimal amount,
        String description,
        LocalDate date,
        CategoryDto category
) {
}
