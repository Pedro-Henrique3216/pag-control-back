package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.InstallmentAccessDeniedException;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayInstallmentUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @InjectMocks
    private PayInstallmentUseCase payInstallmentUseCase;

    @Test
    void shouldMarkInstallmentAsPaidWhenUserAndInstallmentAreValid(){

        User user = new User(
                "John Doe",
                null,
                "testePaidInstallment@gmail.com",
                "password123",
                "1234567890",
                PersonType.PF
        );

        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        Expense expense = new Expense(
                "Test Expense",
                PaymentType.CREDIT,
                LocalDate.now()
        );
        expense.setUser(user);

        Installment installment = new Installment(
                BigDecimal.valueOf(200),
                LocalDate.now().plusDays(10),
                "12345678901234567890123456789012345678901234"
        );

        installment.setExpense(expense);

        Installment installment2 = new Installment(
                BigDecimal.valueOf(200),
                LocalDate.now().plusDays(20),
                "12345678901234567890123456789012345678901234"
        );

        installment2.setExpense(expense);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(installmentRepository.findById(any())).thenReturn(Optional.of(installment));

        payInstallmentUseCase.execute(UUID.randomUUID(), installment.getInstallmentId());

        verify(installmentRepository, times(1)).save(installment);

        assertEquals(InstallmentStatus.PAID, installment.getStatus());
    }

    @Test
    void shouldThrowUserRequiredExceptionWhenUserIdIsNull(){
        assertThrows(
                UserRequiredException.class,
                () -> payInstallmentUseCase.execute(null, UUID.randomUUID())
        );
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> payInstallmentUseCase.execute(UUID.randomUUID(), UUID.randomUUID())
        );
    }

    @Test
    void shouldThrowInstallmentNotFoundExceptionWhenInstallmentDoesNotExist(){
        User user = new User(
                "John Doe",
                null,
                "testePaidInstallment@gmail.com",
                "password123",
                "1234567890",
                PersonType.PF
        );

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(installmentRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(
                InstallmentNotFoundException.class,
                () -> payInstallmentUseCase.execute(UUID.randomUUID(), UUID.randomUUID())
        );
    }

    @Test
    void shouldThrowInstallmentAccessDaniedExceptionWhenInstallmentDoesNotBelongToUser(){
        User user = new User(
                "John Doe",
                null,
                "testePaidInstallment@gmail.com",
                "password123",
                "1234567890",
                PersonType.PF
        );

        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        User user2 = new User(
                "John Doe",
                null,
                "testePaidInstallment@gmail.com",
                "password123",
                "1234567890",
                PersonType.PF
        );

        ReflectionTestUtils.setField(user2, "id", UUID.randomUUID());

        Expense expense = new Expense(
                "Test Expense",
                PaymentType.CREDIT,
                LocalDate.now()
        );
        expense.setUser(user);

        Installment installment = new Installment(
                BigDecimal.valueOf(200),
                LocalDate.now().plusDays(10),
                "12345678901234567890123456789012345678901234"
        );

        installment.setExpense(expense);


        when(userRepository.findById(any())).thenReturn(Optional.of(user2));
        when(installmentRepository.findById(any())).thenReturn(Optional.of(installment));

        assertThrows(
                InstallmentAccessDeniedException.class,
                () -> payInstallmentUseCase.execute(UUID.randomUUID(), installment.getInstallmentId())
        );

        verify(installmentRepository, never()).save(installment);

    }

}