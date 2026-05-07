package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListSuppliersUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;
    @InjectMocks
    private ListSuppliersUseCase listSuppliersUseCase;


    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {

        assertThrows(
                UserRequiredException.class,
                () -> listSuppliersUseCase.execute(null)
        );

        verifyNoInteractions(supplierRepository);
    }


    @Test
    void shouldReturnSuppliersWhenUserExists() {

        UUID userId = UUID.randomUUID();

        List<Supplier> suppliers = List.of(
                new Supplier("Supplier 1", null, new User()),
                new Supplier("Supplier 2", null, new User())
        );

        when(supplierRepository.findAllByUser_Id(userId)).thenReturn(suppliers);

        List<Supplier> result = listSuppliersUseCase.execute(userId);

        assertEquals(2, result.size());

        verify(supplierRepository).findAllByUser_Id(userId);
    }


}