package com.pedrohenrique.pagcontrolback.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendConfirmationEmailRequest(
        @Email(message = "Email not valid")
        @NotBlank(message = "Email is required")
        String email
) {}
