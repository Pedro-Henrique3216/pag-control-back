package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.helper.CategoryDto;
import com.pedrohenrique.pagcontrolback.dtos.response.IncomeResponseDto;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.Income;

public class IncomeMapper {

    public static IncomeResponseDto toDto(Income income) {
        Category category = income.getCategory();
        return new IncomeResponseDto(
                income.getId(),
                income.getAmount().value(),
                income.getDescription(),
                income.getDate(),
                category != null ? new CategoryDto(category.getId(), category.getName()) : null
        );
    }
}
