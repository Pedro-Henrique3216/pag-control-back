package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import com.pedrohenrique.pagcontrolback.dtos.command.CreateCategoryCommand;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateCategoryCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.CategoryRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.CategoryResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.CategoryMapper;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.TransactionType;
import com.pedrohenrique.pagcontrolback.usecases.*;
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
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeactivateCategoryUseCase deactivateCategoryUseCase;

    public CategoryController(
            CreateCategoryUseCase createCategoryUseCase,
            ListCategoryUseCase listCategoryUseCase,
            GetCategoryByIdUseCase getCategoryByIdUseCase,
            UpdateCategoryUseCase updateCategoryUseCase,
            DeactivateCategoryUseCase deactivateCategoryUseCase
    ) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.listCategoryUseCase = listCategoryUseCase;
        this.getCategoryByIdUseCase = getCategoryByIdUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deactivateCategoryUseCase = deactivateCategoryUseCase;
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

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CategoryRequestDto categoryRequest
    ) {
        UpdateCategoryCommand updateCategoryCommand = new UpdateCategoryCommand(
                categoryRequest.name(),
                categoryRequest.categoryType()
        );

        Category category = updateCategoryUseCase.updateCategory(categoryId, user.getId(), updateCategoryCommand);
        return ResponseEntity.ok(CategoryMapper.transformCategoryToCategoryResponseDto(category));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        deactivateCategoryUseCase.deactivateCategory(categoryId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
