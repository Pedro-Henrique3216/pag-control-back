package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateCategoryCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.CategoryRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.CategoryResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.CategoryMapper;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.TransactionType;
import com.pedrohenrique.pagcontrolback.usecases.CreateCategoryUseCase;
import com.pedrohenrique.pagcontrolback.usecases.GetCategoryByIdUseCase;
import com.pedrohenrique.pagcontrolback.usecases.ListCategoryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final ListCategoryUseCase listCategoryUseCase;
    private final GetCategoryByIdUseCase getCategoryByIdUseCase;

    public CategoryController(
            CreateCategoryUseCase createCategoryUseCase,
            ListCategoryUseCase listCategoryUseCase,
            GetCategoryByIdUseCase getCategoryByIdUseCase
    ) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.listCategoryUseCase = listCategoryUseCase;
        this.getCategoryByIdUseCase = getCategoryByIdUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @RequestBody @Valid CategoryRequestDto categoryRequest,
            @AuthenticationPrincipal UserPrincipal user,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        CreateCategoryCommand command = new CreateCategoryCommand(
                categoryRequest.name(),
                categoryRequest.categoryType(),
                user.getId()
        );
        Category categorySaved = createCategoryUseCase.execute(command);
        URI uri = uriComponentsBuilder.path("/categories/{id}").buildAndExpand(categorySaved.getId()).toUri();
        return ResponseEntity.created(uri).body(CategoryMapper.transformCategoryToCategoryResponseDto(categorySaved));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) TransactionType type
    ) {
        List<CategoryResponseDto> categories = listCategoryUseCase.execute(user.getId(), type)
                .stream()
                .map(CategoryMapper::transformCategoryToCategoryResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Category category = getCategoryByIdUseCase.findCategoryById(categoryId, user.getId());
        return ResponseEntity.ok(CategoryMapper.transformCategoryToCategoryResponseDto(category));
    }
}
