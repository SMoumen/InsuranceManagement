package com.insurance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractResponseDTO(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal costAmount
) {}
