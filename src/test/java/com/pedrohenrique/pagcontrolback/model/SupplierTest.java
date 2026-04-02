package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.InvalidSupplierCnpjException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNameRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplierTest {

    @Test
    void shouldCreateSupplierWithOnlyName(){
        Supplier supplier = new Supplier("Supplier A", null, new User());
        assertNotNull(supplier);
        assertEquals("Supplier A", supplier.getName());
        assertNull(supplier.getCnpj());
    }

    @Test
    void shouldCreateSupplierWithNameAndValidCnpj(){
        Supplier supplier = new Supplier("Supplier B", "12.345.678/0001-95", new User());
        assertNotNull(supplier);
        assertEquals("Supplier B", supplier.getName());
        assertEquals("12345678000195", supplier.getCnpj());
    }

    @Test
    void shouldThrowSupplierNameRequiredExceptionWhenNameIsNull(){
        SupplierNameRequiredException exception = assertThrows(SupplierNameRequiredException.class, () -> {
            new Supplier(null, null, new User());
        });
        assertEquals("Supplier name cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowSupplierNameRequiredExceptionWhenNameIsBlank(){
        SupplierNameRequiredException exception = assertThrows(SupplierNameRequiredException.class, () -> {
            new Supplier("   ", null, new User());
        });
        assertEquals("Supplier name cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowUserRequiredExceptionWhenSettingNullUser(){
        Supplier supplier = new Supplier("Supplier C",  null, new User());
        assertThrows(UserRequiredException.class, () -> {
            supplier.setUser(null);
        });
    }

    @Test
    void shouldAcceptNullCnpj(){
        Supplier supplier = new Supplier("Supplier D", null, new User());
        assertNotNull(supplier);
        assertEquals("Supplier D", supplier.getName());
        assertNull(supplier.getCnpj());
    }

    @Test
    void shouldAcceptBlankCnpj(){
        Supplier supplier = new Supplier("Supplier E", "   ", new User());
        assertNotNull(supplier);
        assertEquals("Supplier E", supplier.getName());
        assertNull(supplier.getCnpj());
    }

    @Test
    void shouldRemoveNonDigitsFromCnpj(){
        Supplier supplier = new Supplier("Supplier F", "12.345.678/0001-95", new User());
        assertNotNull(supplier);
        assertEquals("Supplier F", supplier.getName());
        assertEquals("12345678000195", supplier.getCnpj());
    }

    @Test
    void shouldThrowInvalidSupplierCnpjExceptionWhenCnpjIsInvalid(){
        InvalidSupplierCnpjException exception = assertThrows(InvalidSupplierCnpjException.class, () -> {
            new Supplier("Supplier G", "invalid-cnpj", new User());
        });
        assertEquals("Invalid CNPJ format.", exception.getMessage());
    }

}