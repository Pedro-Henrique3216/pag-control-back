package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.SupplierAlreadyExistsWithCnpjException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSupplierUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateSupplierUseCase createSupplierUseCase;

    @Test
    void shouldCreateSupplierWhenDataIsValid(){
        Supplier supplier = new Supplier(
                "Supplier Name"
        );
        User user = new User(
                "John Doe",
                "JD Supplies",
                "test@gmail.com",
                "12345678",
                "111-222-3333",
                PersonType.PJ
        );

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        Supplier createdSupplier = createSupplierUseCase.execute(supplier, UUID.randomUUID());

        verify(supplierRepository, times(1)).save(supplier);

        assertNotNull(createdSupplier);
        assertEquals(supplier.getName(), createdSupplier.getName());
    }

    @Test
    void shouldThrowSupplierRequiredExceptionWhenSupplierIsNull(){
        UUID userId = UUID.randomUUID();

        SupplierRequiredException exception = assertThrows(SupplierRequiredException.class, () -> {
            createSupplierUseCase.execute(null, userId);
        });

        verify(supplierRepository, never()).save(any(Supplier.class));

        assertEquals("Supplier cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowUserIdRequiredExceptionWhenUserIdIsNull(){
        Supplier supplier = new Supplier(
                "Supplier Name"
        );

        UserIdRequiredException exception = assertThrows(UserIdRequiredException.class, () -> {
            createSupplierUseCase.execute(supplier, null);
        });

        verify(supplierRepository, never()).save(any(Supplier.class));

        assertEquals("User ID cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){
        Supplier supplier = new Supplier(
                "Supplier Name"
        );
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            createSupplierUseCase.execute(supplier, userId);
        });

        verify(supplierRepository, never()).save(any(Supplier.class));

        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }

    @Test
    void shouldThrowSupplierAlreadyExistsWithCnpjExceptionWhenCnpjAlreadyExistsForUser(){
        Supplier supplier = new Supplier(
                "Supplier Name"
        );
        supplier.setCnpj("97.958.900/0001-88");
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(any())).thenReturn(Optional.of(new User()));
        when(supplierRepository.existsSupplierByCnpjAndUser_Id(anyString(), any())).thenReturn(true);

        SupplierAlreadyExistsWithCnpjException exception = assertThrows(SupplierAlreadyExistsWithCnpjException.class, () -> {
            createSupplierUseCase.execute(supplier, userId);
        });

        verify(supplierRepository, never()).save(any(Supplier.class));

        assertEquals("Supplier with this CNPJ already exists for this user.", exception.getMessage());
    }

}