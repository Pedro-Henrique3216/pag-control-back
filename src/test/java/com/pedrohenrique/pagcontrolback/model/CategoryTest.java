package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.CategoryNameInvalidException;
import com.pedrohenrique.pagcontrolback.exceptions.CategoryTypeInvalidException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryTest {

    @Test
    void shouldCreateCategorySuccessfully() {
        Category category = new Category("Food", TransactionType.EXPENSE, new User());

        assertEquals("food", category.getName());
        assertEquals(TransactionType.EXPENSE, category.getCategoryType());
    }

    @Test
    void shouldNormalizeName() {
        Category category = new Category("   Food   ", TransactionType.EXPENSE, new User());

        assertEquals("food", category.getName());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(CategoryNameInvalidException.class, () ->
            new Category(null, TransactionType.EXPENSE, new User())
        );
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThrows(CategoryNameInvalidException.class, () ->
            new Category("   ", TransactionType.EXPENSE, new User())
        );
    }

    @Test
    void shouldReturnCorrectCategoryType() {
        Category category = new Category("salary", TransactionType.INCOME, new User());

        assertEquals(TransactionType.INCOME, category.getCategoryType());
    }

    @Test
    void shouldThrowExceptionWhenCategoryTypeIsNull() {
        assertThrows(CategoryTypeInvalidException.class, () ->
            new Category("Food", null, new User())
        );
    }

}