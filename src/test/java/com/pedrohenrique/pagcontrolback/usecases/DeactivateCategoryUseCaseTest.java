package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeactivateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DeactivateCategoryUseCase deactivateCategoryUseCase;

    private UUID categoryId;
    private UUID userId;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        userId = UUID.randomUUID();

        category = mock(Category.class);
    }

    @Test
    void shouldDeactivateCategorySuccessfully() {
        when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        assertDoesNotThrow(() ->
                deactivateCategoryUseCase.deactivateCategory(categoryId, userId)
        );

        verify(categoryRepository).findCategoryByIdAndUserId(categoryId, userId);
        verify(category).deactivate();
        verify(categoryRepository).save(category);
    }

    @Test
    void shouldThrowExceptionWhenCategoryIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deactivateCategoryUseCase.deactivateCategory(null, userId)
        );

        assertEquals("category id cannot be null", exception.getMessage());

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deactivateCategoryUseCase.deactivateCategory(categoryId, null)
        );

        assertEquals("user id cannot be null", exception.getMessage());

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistOrDoesNotBelongToUser() {
        when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> deactivateCategoryUseCase.deactivateCategory(categoryId, userId)
        );

        assertEquals("category not found", exception.getMessage());

        verify(categoryRepository).findCategoryByIdAndUserId(categoryId, userId);
        verify(categoryRepository, never()).save(any());
    }
}