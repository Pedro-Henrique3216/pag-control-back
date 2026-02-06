package com.pedrohenrique.pagcontrolback.config.exceptions;

import com.pedrohenrique.pagcontrolback.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            EmailAlreadyInUseException.class,
            ExpenseDateInTheFutureException.class,
            ExpenseDateRequiredException.class,
            InstallmentDueDateBeforeExpenseDateException.class,
            InstallmentDueDateRequiredException.class,
            InstallmentRequiredException.class,
            InstallmentsRequiredForPaymentTypeException.class,
            InvalidInstallmentAmountException.class,
            InvalidInstallmentDueDateForPaymentTypeException.class,
            InvalidInstallmentDueInDaysException.class,
            MultipleInstallmentsNotAllowedForPaymentTypeException.class,
            UserDomainException.class,
            ExpenseRequiredException.class,
            PaymentTypeRequiredException.class,
            InvalidExpenseAmountException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<HandleExceptionInternalDto> handleException(RuntimeException ex){
        return ResponseEntity.badRequest().body(new HandleExceptionInternalDto(List.of(ex.getMessage()), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<HandleExceptionInternalDto> handleValidationException(MethodArgumentNotValidException ex){
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).toList();
        HandleExceptionInternalDto errorsDto = new HandleExceptionInternalDto(
                errors,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(errorsDto);
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            SupplierNotFoundException.class,
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<HandleExceptionInternalDto> handleNotFoundException(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HandleExceptionInternalDto(List.of(ex.getMessage()), HttpStatus.NOT_FOUND.value(), LocalDateTime.now()));
    }
}
