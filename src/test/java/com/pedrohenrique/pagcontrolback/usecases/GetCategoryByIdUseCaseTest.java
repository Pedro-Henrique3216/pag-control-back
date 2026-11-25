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
class GetCategoryByIdUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private GetCategoryByIdUseCase getCategoryByIdUseCase;

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
    void shouldReturnCategoryWhenCategoryExistsAndBelongsToUser() {
        when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        Category result = getCategoryByIdUseCase.findCategoryById(categoryId, userId);

        assertNotNull(result);
        assertEquals(category, result);

        verify(categoryRepository).findCategoryByIdAndUserId(categoryId, userId);
    }

    @Test
    void shouldThrowExceptionWhenCategoryIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getCategoryByIdUseCase.findCategoryById(null, userId)
        );

        assertEquals("category id cannot be null", exception.getMessage());

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getCategoryByIdUseCase.findCategoryById(categoryId, null)
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
                () -> getCategoryByIdUseCase.findCategoryById(categoryId, userId)
        );

        assertEquals("category not found", exception.getMessage());

        verify(categoryRepository).findCategoryByIdAndUserId(categoryId, userId);
    }
}