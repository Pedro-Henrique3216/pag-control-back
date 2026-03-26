package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Category;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ListCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private ListCategoryUseCase listCategoryUseCase;

    @Test
    void shouldReturnCategoriesWhenUserHaveCategories(){
        Category category = new Category();
        Category category2 = new Category();
        Category category3 = new Category();

        when(categoryRepository.findCategoriesByUserId(any())).thenReturn(List.of(category, category2, category3));

        List<Category> categories = listCategoryUseCase.execute(UUID.randomUUID());

        assertEquals(3, categories.size());
    }

    @Test
    void shouldReturnListEmptyWhenUserDoesHaveCategories(){
        when(categoryRepository.findCategoriesByUserId(any())).thenReturn(List.of());

        List<Category> categories = listCategoryUseCase.execute(UUID.randomUUID());

        assertTrue(categories.isEmpty());
    }

    @Test
    void shouldThrowUserRequiredExceptionWhenUserIdIsNull(){
       assertThrows(
               UserRequiredException.class,
               () -> listCategoryUseCase.execute(null)
       );
    }

}