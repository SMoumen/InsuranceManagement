package com.insurance.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PersonDTO(
        Long id,

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{10,14}$", message = "Phone number must be valid (E.164 format)")
        String phone,

        @NotNull(message = "Birthdate is required")
        @Past(message = "Birthdate must be in the past")
        LocalDate birthdate
) implements ClientDTO {}
