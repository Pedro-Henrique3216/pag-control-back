package com.pedrohenrique.pagcontrolback.usecases;

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
class DeactivateSupplierUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private User user;

    @InjectMocks
    private DeactivateSupplierUseCase deactivateSupplierUseCase;

    private UUID supplierId;
    private UUID userId;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();
        userId = UUID.randomUUID();

        supplier = new Supplier(
                "Supplier A",
                null,
                user
        );
    }

    @Test
    void shouldDeactivateSupplierSuccessfully() {
        when(supplierRepository.findByIdAndUser_Id(supplierId, userId))
                .thenReturn(Optional.of(supplier));

        deactivateSupplierUseCase.deactivateSupplier(
                supplierId,
                userId
        );

        assertFalse(supplier.isActive());

        verify(supplierRepository).findByIdAndUser_Id(
                supplierId,
                userId
        );

        verify(supplierRepository).save(supplier);
    }

    @Test
    void shouldThrowWhenSupplierIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> deactivateSupplierUseCase.deactivateSupplier(
                        null,
                        userId
                )
        );

        verifyNoInteractions(supplierRepository);
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> deactivateSupplierUseCase.deactivateSupplier(
                        supplierId,
                        null
                )
        );

        verifyNoInteractions(supplierRepository);
    }

    @Test
    void shouldThrowWhenSupplierDoesNotExist() {
        when(supplierRepository.findByIdAndUser_Id(supplierId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                SupplierNotFoundException.class,
                () -> deactivateSupplierUseCase.deactivateSupplier(
                        supplierId,
                        userId
                )
        );

        verify(supplierRepository).findByIdAndUser_Id(
                supplierId,
                userId
        );

        verify(supplierRepository, never()).save(any(Supplier.class));
    }
}
