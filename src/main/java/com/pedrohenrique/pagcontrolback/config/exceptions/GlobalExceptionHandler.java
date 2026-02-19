package com.pedrohenrique.pagcontrolback.config.exceptions;

import com.pedrohenrique.pagcontrolback.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
            InvalidExpenseAmountException.class,
            InvalidSupplierCnpjException.class,
            SupplierNameRequiredException.class,
            SupplierRequiredException.class,
            UserIdRequiredException.class,
            UserRequiredException.class,
            FutureMonthNotAllowedException.class
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
            InstallmentNotFoundException.class,
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<HandleExceptionInternalDto> handleNotFoundException(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HandleExceptionInternalDto(List.of(ex.getMessage()), HttpStatus.NOT_FOUND.value(), LocalDateTime.now()));
    }

    @ExceptionHandler({
            SupplierAlreadyExistsWithCnpjException.class,
            InstallmentAlreadyPaidException.class,
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<HandleExceptionInternalDto> handleConflictException(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new HandleExceptionInternalDto(List.of(ex.getMessage()), HttpStatus.CONFLICT.value(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<HandleExceptionInternalDto> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {

        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        String message = "Invalid value for parameter '" + paramName +
                "'. Expected type: " + requiredType;

        HandleExceptionInternalDto error = new HandleExceptionInternalDto(
                List.of(message),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InstallmentAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<HandleExceptionInternalDto> handleAccessDeniedException(InstallmentAccessDeniedException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new HandleExceptionInternalDto(List.of(ex.getMessage()), HttpStatus.FORBIDDEN.value(), LocalDateTime.now()));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            InvalidTokenException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<HandleExceptionInternalDto> handleUnauthorizedExceptio(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HandleExceptionInternalDto(List.of(ex.getMessage()), HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now()));
    }

    @ExceptionHandler(TokenGenerationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<HandleExceptionInternalDto> handleTokenGeneration(TokenGenerationException ex) {
        return ResponseEntity.internalServerError().body(new HandleExceptionInternalDto(List.of(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<HandleExceptionInternalDto> handleGenericException(Exception ex){
        return ResponseEntity.internalServerError().body(new HandleExceptionInternalDto(List.of(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now()));
    }
}
