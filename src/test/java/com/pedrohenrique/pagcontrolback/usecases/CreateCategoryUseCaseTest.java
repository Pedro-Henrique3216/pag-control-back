package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.CategoryAlreadyExistsException;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryRequiredException;
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

        UUID userId = UUID.randomUUID();

        assertThrows(CategoryRequiredException.class, () -> {
            createCategoryUseCase.execute(null, userId);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {

        Category category = new Category("food", CategoryType.EXPENSE);

        assertThrows(UserRequiredException.class, () -> {
            createCategoryUseCase.execute(category, null);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        UUID userId = UUID.randomUUID();
        Category category = new Category("food", CategoryType.EXPENSE);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            createCategoryUseCase.execute(category, userId);
        });
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {

        UUID userId = UUID.randomUUID();
        Category category = new Category("food", CategoryType.EXPENSE);

        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.existsCategoryByNameAndUser_Id("food", userId)).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> {
            createCategoryUseCase.execute(category, userId);
        });
    }

    @Test
    void shouldCreateCategorySuccessfully() {

        UUID userId = UUID.randomUUID();
        Category category = new Category("food", CategoryType.EXPENSE);

        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.existsCategoryByNameAndUser_Id("food", userId)).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);

        Category result = createCategoryUseCase.execute(category, userId);

        assertNotNull(result);
        assertEquals(user, category.getUser());

        verify(categoryRepository, times(1)).save(category);
    }
}
