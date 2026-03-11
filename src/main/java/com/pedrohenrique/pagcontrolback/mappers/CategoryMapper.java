package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.request.CategoryRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.CategoryResponseDto;
import com.pedrohenrique.pagcontrolback.model.Category;

public class CategoryMapper {

    public static Category toDomain(CategoryRequestDto categoryRequestDto) {
        return new Category(
            categoryRequestDto.name(),
            categoryRequestDto.categoryType()
        );
    }

    public static CategoryResponseDto fromDomain(Category category) {
        return new CategoryResponseDto(
            category.getId(),
            category.getName(),
            category.getCategoryType()
        );
    }
}
