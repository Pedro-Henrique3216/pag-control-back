package com.pedrohenrique.pagcontrolback.dtos.request;

import com.pedrohenrique.pagcontrolback.model.PersonType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserRequestDto(
        @NotBlank
        String name,
        String fantasyName,
        @NotBlank
        @Email(message = "Email not valid")
        String email,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        )
        String password,
        @NotBlank
        @Pattern(
                regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$",
                message = "Phone not valid"
        )
        String phone,
        @NotNull
        PersonType personType
) {
}
