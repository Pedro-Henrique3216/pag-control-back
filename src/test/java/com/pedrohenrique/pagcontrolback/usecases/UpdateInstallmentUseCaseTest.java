package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.ValueObjects.Money;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateInstallmentCommand;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentAccessDeniedException;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.*;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import org.junit.jupiter.api.Nested;
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

    @InjectMocks
    private UpdateInstallmentUseCase updateInstallmentUseCase;

    private User createUser() {
        User user = new User(
                "John Doe",
                null,
                "teste@gmail.com",
                "hashed-password",
                "11999999999",
                PersonType.PF
        );

        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        return user;
    }

    private Expense createExpense(User user) {
        return new Expense(
                "INV-001",
                "Teste",
                PaymentType.CREDIT,
                LocalDate.now(),
                user,
                Money.of(new BigDecimal("200.00"))
        );
    }

    private Installment createInstallment(Expense expense) {
        return new Installment(
                Money.of(new BigDecimal("200.00")),
                LocalDate.now().plusDays(10),
                "123456789",
                expense,
                1,
                1
        );
    }

    @Nested
    class SuccessTests {

        @Test
        void shouldUpdateInstallmentWhenUserAndInstallmentAreValid() {

            User user = createUser();

            Expense expense = createExpense(user);

            Installment installment = createInstallment(expense);

            UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                    new BigDecimal("100.00"),
                    LocalDate.now().plusDays(20),
                    "999999999",
                    user.getId(),
                    UUID.randomUUID()
            );

            when(installmentRepository.findById(any()))
                    .thenReturn(Optional.of(installment));

            updateInstallmentUseCase.execute(command);

            verify(installmentRepository, times(1))
                    .save(any(Installment.class));
        }
    }

    @Nested
    class ValidationTests {

        @Test
        void shouldThrowUserRequiredExceptionWhenUserIdIsNull() {

            UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                    null,
                    null,
                    null,
                    null,
                    UUID.randomUUID()
            );

            assertThrows(
                    UserRequiredException.class,
                    () -> updateInstallmentUseCase.execute(command)
            );
        }

        @Test
        void shouldThrowInstallmentRequiredExceptionWhenInstallmentIdIsNull() {

            UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                    null,
                    null,
                    null,
                    UUID.randomUUID(),
                    null
            );

            assertThrows(
                    InstallmentRequiredException.class,
                    () -> updateInstallmentUseCase.execute(command)
            );
        }
    }

    @Nested
    class ErrorTests {

        @Test
        void shouldThrowInstallmentNotFoundExceptionWhenInstallmentDoesNotExist() {

            User user = createUser();

            UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                    null,
                    null,
                    null,
                    user.getId(),
                    UUID.randomUUID()
            );

            when(installmentRepository.findById(any()))
                    .thenReturn(Optional.empty());

            assertThrows(
                    InstallmentNotFoundException.class,
                    () -> updateInstallmentUseCase.execute(command)
            );
        }

        @Test
        void shouldThrowInstallmentAccessDeniedExceptionWhenInstallmentDoesNotBelongToUser() {

            User owner = createUser();
            User anotherUser = createUser();

            ReflectionTestUtils.setField(
                    anotherUser,
                    "id",
                    UUID.randomUUID()
            );

            Expense expense = createExpense(owner);

            Installment installment = createInstallment(expense);

            UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                    null,
                    null,
                    null,
                    anotherUser.getId(),
                    UUID.randomUUID()
            );

            when(installmentRepository.findById(any()))
                    .thenReturn(Optional.of(installment));

            assertThrows(
                    InstallmentAccessDeniedException.class,
                    () -> updateInstallmentUseCase.execute(command)
            );
        }
    }
}