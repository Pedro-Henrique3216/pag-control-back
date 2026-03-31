package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateCategoryCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryAlreadyExistsException;
import com.pedrohenrique.pagcontrolback.exceptions.CreateCategoryCommandRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.model.CategoryType;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
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
class CreateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CreateCategoryUseCase createCategoryUseCase;

    @Test
    void shouldThrowExceptionWhenCategoryIsNull() {

        assertThrows(CreateCategoryCommandRequiredException.class, () -> {
            createCategoryUseCase.execute(null);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {

        CreateCategoryCommand command = new CreateCategoryCommand(
                "food",
                CategoryType.EXPENSE,
                null
        );

        assertThrows(UserRequiredException.class, () -> {
            createCategoryUseCase.execute(command);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        UUID userId = UUID.randomUUID();
        CreateCategoryCommand command = new CreateCategoryCommand(
                "food",
                CategoryType.EXPENSE,
                userId
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            createCategoryUseCase.execute(command);
        });
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {

        UUID userId = UUID.randomUUID();
        CreateCategoryCommand command = new CreateCategoryCommand(
                "food",
                CategoryType.EXPENSE,
                userId
        );

        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.existsCategoryByNameIgnoreCaseAndUserId("food", command.userId())).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> {
            createCategoryUseCase.execute(command);
        });
    }

    @Test
    void shouldCreateCategorySuccessfully() {

        UUID userId = UUID.randomUUID();
        CreateCategoryCommand command = new CreateCategoryCommand(
                "food",
                CategoryType.EXPENSE,
                userId
        );

        User user = new User();

        Category category = new Category(
                "food",
                CategoryType.EXPENSE,
                user
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.existsCategoryByNameIgnoreCaseAndUserId("food", userId)).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);

        Category result = createCategoryUseCase.execute(command);

        assertNotNull(result);
        assertEquals(user, category.getUser());

        verify(categoryRepository, times(1)).save(category);
    }
}
