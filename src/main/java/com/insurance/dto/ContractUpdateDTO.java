package com.insurance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ContractUpdateDTO(
        @NotNull(message = "Cost amount is required")
        @DecimalMin(value = "0.01", message = "Cost amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "Cost amount must have at most 2 decimal places")
        BigDecimal costAmount
) {}
