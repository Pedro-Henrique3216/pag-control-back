package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.CategoryRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.CategoryResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.CategoryMapper;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.usecases.CreateCategoryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @RequestBody @Valid CategoryRequestDto categoryRequest,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        Category category = CategoryMapper.toDomain(categoryRequest);
        Category categorySaved = createCategoryUseCase.execute(category, user.getId());
        URI uri = uriComponentsBuilder.path("/categories/{id}").buildAndExpand(categorySaved.getId()).toUri();
        return ResponseEntity.created(uri).body(CategoryMapper.fromDomain(categorySaved));
    }
}
