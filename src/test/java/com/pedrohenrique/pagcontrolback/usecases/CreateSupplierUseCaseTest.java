package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateSupplierCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
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
                "Supplier Name",
                null,
                new User()
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

        CreateSupplierCommand command = new CreateSupplierCommand(
                "Supplier Name",
                null,
                UUID.randomUUID()
        );

        Supplier createdSupplier = createSupplierUseCase.execute(command);

        verify(supplierRepository, times(1)).save(supplier);

        assertNotNull(createdSupplier);
        assertEquals(supplier.getName(), createdSupplier.getName());
    }

    @Test
    void shouldThrowCreateSupplierCommandRequiredExceptionWhenSupplierIsNull(){
        CreateSupplierCommandRequiredException exception = assertThrows(CreateSupplierCommandRequiredException.class, () -> {
            createSupplierUseCase.execute(null);
        });

        verify(supplierRepository, never()).save(any(Supplier.class));

        assertEquals("Create supplier command cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowUserIdRequiredExceptionWhenUserIdIsNull(){

        CreateSupplierCommand command = new CreateSupplierCommand(
                "Supplier Name",
                null,
                null
        );

        UserIdRequiredException exception = assertThrows(UserIdRequiredException.class, () -> {
            createSupplierUseCase.execute(command);
        });

        verify(supplierRepository, never()).save(any(Supplier.class));

        assertEquals("User ID cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){
        UUID userId = UUID.randomUUID();
        CreateSupplierCommand command = new CreateSupplierCommand(
                "Supplier Name",
                null,
                userId
        );


        when(userRepository.findById(any())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            createSupplierUseCase.execute(command);
        });

        verify(supplierRepository, never()).save(any(Supplier.class));

        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }

    @Test
    void shouldThrowSupplierAlreadyExistsWithCnpjExceptionWhenCnpjAlreadyExistsForUser(){
        UUID userId = UUID.randomUUID();

        CreateSupplierCommand command = new CreateSupplierCommand(
                "Supplier Name",
                "97.958.900/0001-88",
                userId
        );

        when(userRepository.findById(any())).thenReturn(Optional.of(new User()));
        when(supplierRepository.existsSupplierByCnpjAndUser_Id(anyString(), any())).thenReturn(true);

        SupplierAlreadyExistsWithCnpjException exception = assertThrows(SupplierAlreadyExistsWithCnpjException.class, () -> {
            createSupplierUseCase.execute(command);
        });

        verify(supplierRepository, never()).save(any(Supplier.class));

        assertEquals("Supplier with this CNPJ already exists for this user.", exception.getMessage());
    }

}