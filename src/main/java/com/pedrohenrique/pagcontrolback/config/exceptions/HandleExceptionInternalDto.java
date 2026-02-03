package com.pedrohenrique.pagcontrolback.config.exceptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record HandleExceptionInternalDto(
        List<String> errors,
        Integer status,
        LocalDateTime timestamp
) {
}
