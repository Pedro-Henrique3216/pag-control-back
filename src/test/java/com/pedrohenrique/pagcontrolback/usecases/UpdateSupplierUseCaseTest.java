package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateSupplierCommand;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
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
class UpdateSupplierUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private User user;

    @InjectMocks
    private UpdateSupplierUseCase updateSupplierUseCase;

    private UUID supplierId;
    private UUID userId;
    private Supplier supplier;
    private UpdateSupplierCommand command;

    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();
        userId = UUID.randomUUID();

        supplier = new Supplier(
                "Fornecedor antigo",
                "11222333000181",
                user
        );

        command = new UpdateSupplierCommand(
                "Fornecedor atualizado",
                "11222333000181"
        );
    }

    @Test
    void shouldUpdateSupplierSuccessfully() {
        when(supplierRepository.findByIdAndUser_Id(supplierId, userId))
                .thenReturn(Optional.of(supplier));

        Supplier result = updateSupplierUseCase.updateSupplier(
                supplierId,
                userId,
                command
        );

        assertNotNull(result);
        assertEquals("Fornecedor atualizado", result.getName());
        assertNotNull(result.getUpdatedAt());

        verify(supplierRepository).findByIdAndUser_Id(supplierId, userId);
        verify(supplierRepository).save(supplier);
    }

    @Test
    void shouldThrowExceptionWhenSupplierIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> updateSupplierUseCase.updateSupplier(
                        null,
                        userId,
                        command
                )
        );

        verifyNoInteractions(supplierRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> updateSupplierUseCase.updateSupplier(
                        supplierId,
                        null,
                        command
                )
        );

        verifyNoInteractions(supplierRepository);
    }

    @Test
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> updateSupplierUseCase.updateSupplier(
                        supplierId,
                        userId,
                        null
                )
        );

        verifyNoInteractions(supplierRepository);
    }

    @Test
    void shouldThrowExceptionWhenSupplierDoesNotExist() {
        when(supplierRepository.findByIdAndUser_Id(supplierId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                SupplierNotFoundException.class,
                () -> updateSupplierUseCase.updateSupplier(
                        supplierId,
                        userId,
                        command
                )
        );

        verify(supplierRepository).findByIdAndUser_Id(supplierId, userId);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }
}