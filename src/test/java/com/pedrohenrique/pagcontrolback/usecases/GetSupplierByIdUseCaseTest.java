package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.PersonType;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSupplierByIdUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private GetSupplierByIdUseCase getSupplierByIdUseCase;


    @Test
    void shouldReturnSupplierWhenUserAndSupplierAreValid(){
        User user = new User(
                "John Doe",
                null,
                "testeSupplier@gmail.com",
                "password",
                "12345678901",
                PersonType.PF
        );

        Supplier supplier = new Supplier(
                "Supplier 1",
                null,
                user
        );

        when(userRepository.existsById(any())).thenReturn(true);
        when(supplierRepository.findByIdAndUser_Id(any(), any())).thenReturn(Optional.of(supplier));

        assertEquals(supplier, getSupplierByIdUseCase.execute(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void shouldThrowUserRequiredExceptionWhenUserIdIsNull(){
        assertThrows(UserRequiredException.class,
                () -> getSupplierByIdUseCase.execute(null, UUID.randomUUID()));
    }

    @Test
    void shouldThrowSupplierRequiredExceptionWhenSupplierIdIsNull(){
        assertThrows(SupplierRequiredException.class,
                () -> getSupplierByIdUseCase.execute(UUID.randomUUID(), null));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){
        when(userRepository.existsById(any())).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> getSupplierByIdUseCase.execute(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void shouldThrowSupplierNotFoundExceptionWhenSupplierIsNotFoundForUser(){
        when(userRepository.existsById(any())).thenReturn(true);
        when(supplierRepository.findByIdAndUser_Id(any(), any())).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class,
                () -> getSupplierByIdUseCase.execute(UUID.randomUUID(), UUID.randomUUID()));
    }


}