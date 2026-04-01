package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateInstallmentCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateInstallmentUseCaseTest {

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateInstallmentUseCase updateInstallmentUseCase;

    @Test
    void shouldUpdateInstallmentWhenUserAndInstallmentAreValid(){
        User user = new User(
                "John Doe",
                null,
                "testeUpdateInstallment@gmail.com",
                "password123",
                "1234567890",
                PersonType.PF
        );

        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        Expense expense = new Expense(
                "Test Expense",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                new Supplier()
        );

        Installment installment = new Installment(
                BigDecimal.valueOf(200),
                LocalDate.now().plusDays(10),
                "12345678901234567890123456789012345678901234"
        );

       expense.addInstallment(installment);

        UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                BigDecimal.valueOf(100),
                LocalDate.now().plusDays(20),
                "12345678901234567890123456789012345678901234",
                user.getId(),
                UUID.randomUUID()
        );

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(installmentRepository.findById(any())).thenReturn(Optional.of(installment));

        updateInstallmentUseCase.execute(command);

        verify(installmentRepository, times(1)).save(any(Installment.class));
    }

    @Test
    void shouldThrowUserRequiredExceptionWhenUserIdIsNull(){

        UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(UserRequiredException.class, () -> {
            updateInstallmentUseCase.execute(command);
        });
    }

    @Test
    void shouldThrowInstallmentRequiredExceptionWhenInstallmentIdIsNull(){

        UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                null,
                null,
                null,
                UUID.randomUUID(),
                null
        );

        assertThrows(InstallmentRequiredException.class, () -> {
            updateInstallmentUseCase.execute(command);
        });
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){

        UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                null,
                null,
                null,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            updateInstallmentUseCase.execute(command);
        });
    }

    @Test
    void shouldThrowInstallmentNotFoundExceptionWhenInstallmentDoesNotExist(){

        User user = new User(
                "John Doe",
                null,
                "testeUpdateInstallment@gmail.com",
                "password123",
                "1234567890",
                PersonType.PF
        );

        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                null,
                null,
                null,
                user.getId(),
                UUID.randomUUID()
        );

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(installmentRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(InstallmentNotFoundException.class, () -> {
            updateInstallmentUseCase.execute(command);
        });
    }

    @Test
    void shouldThrowInstallmentAccessDeniedExceptionWhenInstallmentDoesNotBelongToUser(){

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
                LocalDate.now(),
                user,
                new Supplier()
        );

        Installment installment = new Installment(
                BigDecimal.valueOf(200),
                LocalDate.now().plusDays(10),
                "12345678901234567890123456789012345678901234"
        );

       expense.addInstallment(installment);

       UpdateInstallmentCommand command = new UpdateInstallmentCommand(
               null,
               null,
               null,
               user2.getId(),
               UUID.randomUUID()
       );

        when(userRepository.findById(any())).thenReturn(Optional.of(user2));
        when(installmentRepository.findById(any())).thenReturn(Optional.of(installment));

        assertThrows(InstallmentAccessDeniedException.class, () -> {
            updateInstallmentUseCase.execute(command);
        });
    }

}