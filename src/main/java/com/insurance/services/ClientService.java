package com.insurance.services;

import com.insurance.dto.*;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.models.*;
import com.insurance.repository.ClientRepository;
import com.insurance.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClientService {
    private final ClientRepository clientRepository;
    private final ContractRepository contractRepository;

    public ClientDTO createClient(ClientDTO clientDTO) {
        Client client;
        if (clientDTO instanceof PersonDTO personDTO) {
            Person person = new Person();
            person.setName(personDTO.name());
            person.setEmail(personDTO.email());
            person.setPhone(personDTO.phone());
            person.setBirthdate(personDTO.birthdate());
            client = person;
        } else if (clientDTO instanceof CompanyDTO companyDTO) {
            Company company = new Company();
            company.setName(companyDTO.name());
            company.setEmail(companyDTO.email());
            company.setPhone(companyDTO.phone());
            company.setCompanyIdentifier(companyDTO.companyIdentifier());
            client = company;
        } else {
            throw new IllegalArgumentException("Unknown client type");
        }

        client = clientRepository.save(client);
        return mapToDTO(client);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return mapToDTO(client);
    }

    public ClientDTO updateClient(Long id, ClientUpdateDTO updateDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        client.setName(updateDTO.name());
        client.setEmail(updateDTO.email());
        client.setPhone(updateDTO.phone());

        client = clientRepository.save(client);
        return mapToDTO(client);
    }

    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        // Update end dates of all contracts
        LocalDate currentDate = LocalDate.now();
        List<Contract> contracts = contractRepository.findByClientId(id);
        contracts.forEach(contract -> {
            if (contract.getEndDate() == null || contract.getEndDate().isAfter(currentDate)) {
                contract.setEndDate(currentDate);
            }
        });
        var savedContracts = contractRepository.saveAll(contracts);
        if(savedContracts.isEmpty() || savedContracts.size() != contracts.size()) {
            log.error("Contracts were not correctly saved for client {}", id);
        }

        clientRepository.delete(client);
    }

    private ClientDTO mapToDTO(Client client) {
        if (client instanceof Person person) {
            return new PersonDTO(
                    person.getId(),
                    person.getName(),
                    person.getEmail(),
                    person.getPhone(),
                    person.getBirthdate()
            );
        } else if (client instanceof Company company) {
            return new CompanyDTO(
                    company.getId(),
                    company.getName(),
                    company.getEmail(),
                    company.getPhone(),
                    company.getCompanyIdentifier()
            );
        }
        throw new IllegalArgumentException("Unknown client type");
    }
}