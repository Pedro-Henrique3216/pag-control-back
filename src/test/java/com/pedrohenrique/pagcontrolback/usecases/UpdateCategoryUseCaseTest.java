package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateCategoryCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.TransactionType;
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
class UpdateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private UpdateCategoryUseCase updateCategoryUseCase;

    private UUID categoryId;
    private UUID userId;
    private Category category;
    private UpdateCategoryCommand command;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        userId = UUID.randomUUID();

        category = mock(Category.class);

        command = new UpdateCategoryCommand(
                "Food",
                TransactionType.EXPENSE
        );
    }

    @Test
    void shouldUpdateCategorySuccessfully() {
        when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        Category result = updateCategoryUseCase.updateCategory(
                categoryId,
                userId,
                command
        );

        assertNotNull(result);
        assertEquals(category, result);

        verify(categoryRepository).findCategoryByIdAndUserId(categoryId, userId);

        verify(category).update(
                command.name(),
                command.categoryType()
        );

        verify(categoryRepository).save(category);
    }

    @Test
    void shouldThrowExceptionWhenCategoryIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateCategoryUseCase.updateCategory(
                        null,
                        userId,
                        command
                )
        );

        assertEquals("category id cannot be null", exception.getMessage());

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateCategoryUseCase.updateCategory(
                        categoryId,
                        null,
                        command
                )
        );

        assertEquals("user id cannot be null", exception.getMessage());

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenCommandIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateCategoryUseCase.updateCategory(
                        categoryId,
                        userId,
                        null
                )
        );

        assertEquals("update category command cannot be null", exception.getMessage());

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistOrDoesNotBelongToUser() {
        when(categoryRepository.findCategoryByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> updateCategoryUseCase.updateCategory(
                        categoryId,
                        userId,
                        command
                )
        );

        assertEquals("category not found", exception.getMessage());

        verify(categoryRepository).findCategoryByIdAndUserId(categoryId, userId);

        verify(categoryRepository, never()).save(any());
    }
}