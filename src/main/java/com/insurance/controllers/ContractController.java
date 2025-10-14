package com.insurance.controllers;

import com.insurance.dto.ContractDTO;
import com.insurance.dto.ContractResponseDTO;
import com.insurance.dto.ContractSumDTO;
import com.insurance.dto.ContractUpdateDTO;
import com.insurance.services.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
class ContractController {
    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractResponseDTO> createContract(@Valid @RequestBody ContractDTO contractDTO) {
        ContractResponseDTO created = contractService.createContract(contractDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/cost")
    public ResponseEntity<ContractResponseDTO> updateContractCost(
            @PathVariable Long id,
            @Valid @RequestBody ContractUpdateDTO updateDTO) {
        ContractResponseDTO updated = contractService.updateContractCost(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ContractResponseDTO>> getActiveContracts(
            @PathVariable Long clientId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updateDate) {
        List<ContractResponseDTO> contracts = contractService.getActiveContracts(clientId, updateDate);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/client/{clientId}/sum")
    public ResponseEntity<ContractSumDTO> getActiveContractsSum(@PathVariable Long clientId) {
        ContractSumDTO sum = contractService.getActiveContractsSum(clientId);
        return ResponseEntity.ok(sum);
    }
}