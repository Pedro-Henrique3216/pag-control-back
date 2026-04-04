package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.response.IncomeResponseDto;
import com.pedrohenrique.pagcontrolback.model.Income;

public class IncomeMapper {

    public static IncomeResponseDto toDto(Income income) {
        return new IncomeResponseDto(
                income.getId(),
                income.getAmount(),
                income.getDescription(),
                income.getDate(),
                income.getCategory() != null ? income.getCategory().getId() : null
        );
    }
}
