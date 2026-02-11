package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepositoryCustom;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListInstallmentsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InstallmentRepositoryCustom installmentRepositoryCustom;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ListInstallmentsUseCase listInstallmentsUseCase;

    @Test
    void shouldThrowUserIdRequiredExceptionWhenUserIdIsNull(){
        var exception = assertThrows(RuntimeException.class, () -> listInstallmentsUseCase.execute(null, null));
        assertEquals("User id is required.", exception.getMessage());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){
        var userId = java.util.UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);
        var exception = assertThrows(RuntimeException.class, () -> listInstallmentsUseCase.execute(userId, null));
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void shouldThrowSupplierNotFoundExceptionWhenSupplierDoesNotExist(){
        var userId = java.util.UUID.randomUUID();
        var supplierId = java.util.UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        var query = new ListInstallmentQuery(null, supplierId, null, null, false);
        when(supplierRepository.existsById(supplierId)).thenReturn(false);
        var exception = assertThrows(RuntimeException.class, () -> listInstallmentsUseCase.execute(userId, query));
        assertEquals("Supplier not found", exception.getMessage());
    }

    @Test
    void shouldSearchInstallmentsWhenUserAndFiltersAreValid(){
        var userId = java.util.UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        var query = new ListInstallmentQuery(null, null, null, null, false);
        when(installmentRepositoryCustom.search(query, userId)).thenReturn(java.util.Collections.emptyList());
        var result = listInstallmentsUseCase.execute(userId, query);
        assertNotNull(result);

        verify(installmentRepositoryCustom)
                .search(query, userId);
    }



}