package com.insurance.services;

import com.insurance.dto.*;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.models.*;
import com.insurance.repository.ClientRepository;
import com.insurance.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.insurance.models.Contract;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {
    private final ContractRepository contractRepository;
    private final ClientRepository clientRepository;

    public ContractResponseDTO createContract(ContractDTO contractDTO) {
        Client client = clientRepository.findById(contractDTO.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + contractDTO.clientId()));

        Contract contract = new Contract();
        contract.setClient(client);
        contract.setStartDate(contractDTO.startDate());
        contract.setEndDate(contractDTO.endDate());
        contract.setCostAmount(contractDTO.costAmount());

        contract = contractRepository.save(contract);
        return mapToResponseDTO(contract);
    }

    public ContractResponseDTO updateContractCost(Long id, ContractUpdateDTO updateDTO) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        contract.setCostAmount(updateDTO.costAmount());
        contract = contractRepository.save(contract);

        return mapToResponseDTO(contract);
    }

    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getActiveContracts(Long clientId, LocalDate updateDate) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }

        LocalDate currentDate = LocalDate.now();
        List<Contract> contracts;

        if (updateDate != null) {
            contracts = contractRepository.findActiveContractsByClientIdAndUpdateDate(
                    clientId, currentDate, updateDate);
        } else {
            contracts = contractRepository.findActiveContractsByClientId(clientId, currentDate);
        }

        return contracts.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContractSumDTO getActiveContractsSum(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }

        LocalDate currentDate = LocalDate.now();
        BigDecimal sum = contractRepository.sumActiveContractsCostByClientId(clientId, currentDate);

        return new ContractSumDTO(sum);
    }

    private ContractResponseDTO mapToResponseDTO(Contract contract) {
        return new ContractResponseDTO(
                contract.getId(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getCostAmount()
        );
    }
}