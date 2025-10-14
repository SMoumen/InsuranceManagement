package com.insurance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyDTO(
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

        @NotBlank(message = "Company identifier is required")
        @Pattern(regexp = "^[a-zA-Z]{3}-\\d{3}$", message = "Company identifier must match format: aaa-123")
        String companyIdentifier
) implements ClientDTO {}
