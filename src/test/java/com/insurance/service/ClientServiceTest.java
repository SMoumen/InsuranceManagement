package com.insurance.service;

import com.insurance.dto.*;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.models.*;
import com.insurance.repository.ClientRepository;
import com.insurance.repository.ContractRepository;
import com.insurance.services.ClientService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.insurance.TestHelper.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Client Service Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private ClientService clientService;



    @Nested
    @DisplayName("Create Client Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateClientTests {

        @Test
        @Order(1)
        @DisplayName("Should create person client with valid data")
        void shouldCreatePersonClientSuccessfully() {
            
            var personDTO = createValidPersonDTO(null);
            var savedPerson = createPersonEntity();

            when(clientRepository.save(any(Person.class))).thenReturn(savedPerson);

            
            var result = clientService.createClient(personDTO);

            
            assertThat(result)
                    .isNotNull()
                    .isInstanceOf(PersonDTO.class)
                    .extracting("id", "name", "email", "phone")
                    .containsExactly(TEST_CLIENT_ID, "John Doe", TEST_EMAIL, TEST_PHONE);

            assertThat(((PersonDTO) result).birthdate()).isEqualTo(LocalDate.of(1990, 5, 15));


            var personCaptor = ArgumentCaptor.forClass(Person.class);
            verify(clientRepository).save(personCaptor.capture());

            var capturedPerson = personCaptor.getValue();
            assertThat(capturedPerson)
                    .extracting(Person::getName, Person::getEmail, Person::getPhone)
                    .containsExactly("John Doe", TEST_EMAIL, TEST_PHONE);
        }

        @Test
        @Order(2)
        @DisplayName("Should create company client with valid data")
        void shouldCreateCompanyClientSuccessfully() {
            
            var companyDTO = createValidCompanyDTO(null);
            var savedCompany = createCompanyEntity();

            when(clientRepository.save(any(Company.class))).thenReturn(savedCompany);

            
            var result = clientService.createClient(companyDTO);

            
            assertThat(result)
                    .isNotNull()
                    .isInstanceOf(CompanyDTO.class);

            var companyResult = (CompanyDTO) result;
            assertThat(companyResult)
                    .extracting(CompanyDTO::id, CompanyDTO::name, CompanyDTO::companyIdentifier)
                    .containsExactly(TEST_CLIENT_ID, "Tech Corp", "abc-123");

            verify(clientRepository).save(any(Company.class));
            verifyNoMoreInteractions(clientRepository);
        }

        @Test
        @Order(3)
        @DisplayName("Should throw exception when client DTO is null")
        void shouldThrowExceptionWhenClientDTOIsNull() {

            assertThatThrownBy(() -> clientService.createClient(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown client type");

            verify(clientRepository, never()).save(any());
        }

        @Test
        @Order(4)
        @DisplayName("Should preserve all person fields during creation")
        void shouldPreserveAllPersonFieldsDuringCreation() {
            
            var birthdate = LocalDate.of(1985, 12, 25);
            var personDTO = new PersonDTO(null, "Alice Smith", "alice@test.com", "+33687654321", birthdate);

            var savedPerson = new Person();
            savedPerson.setId(5L);
            savedPerson.setName("Alice Smith");
            savedPerson.setEmail("alice@test.com");
            savedPerson.setPhone("+33687654321");
            savedPerson.setBirthdate(birthdate);

            when(clientRepository.save(any(Person.class))).thenReturn(savedPerson);

            
            var result = (PersonDTO) clientService.createClient(personDTO);

            
            assertThat(result.birthdate()).isEqualTo(birthdate);
            assertThat(result.email()).isEqualTo("alice@test.com");
        }
    }

    @Nested
    @DisplayName("Get Client Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetClientTests {

        @Test
        @Order(1)
        @DisplayName("Should get existing person client by id")
        void shouldGetPersonClientById() {
            
            var person = createPersonEntity();
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(person));

            
            var result = clientService.getClient(TEST_CLIENT_ID);

            
            assertThat(result).isInstanceOf(PersonDTO.class);
            var personDTO = (PersonDTO) result;
            assertThat(personDTO.id()).isEqualTo(TEST_CLIENT_ID);
            assertThat(personDTO.name()).isEqualTo("John Doe");

            verify(clientRepository, times(1)).findById(TEST_CLIENT_ID);
            verifyNoMoreInteractions(clientRepository);
        }

        @Test
        @Order(2)
        @DisplayName("Should get existing company client by id")
        void shouldGetCompanyClientById() {
            
            var company = createCompanyEntity();
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(company));

            
            var result = clientService.getClient(TEST_CLIENT_ID);

            
            assertThat(result).isInstanceOf(CompanyDTO.class);
            var companyDTO = (CompanyDTO) result;
            assertThat(companyDTO.id()).isEqualTo(TEST_CLIENT_ID);
            assertThat(companyDTO.companyIdentifier()).isEqualTo("abc-123");
        }

        @ParameterizedTest
        @MethodSource("invalidClientIds")
        @DisplayName("Should throw exception for non-existent client ids")
        void shouldThrowExceptionWhenClientNotFound(Long invalidId, String expectedMessage) {
            
            when(clientRepository.findById(invalidId)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> clientService.getClient(invalidId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(expectedMessage);

            verify(clientRepository).findById(invalidId);
        }

        static Stream<Arguments> invalidClientIds() {
            return Stream.of(
                    Arguments.of(999L, "Client not found with id: 999"),
                    Arguments.of(0L, "Client not found with id: 0"),
                    Arguments.of(-1L, "Client not found with id: -1")
            );
        }
    }

    @Nested
    @DisplayName("Update Client Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateClientTests {

        @Test
        @Order(1)
        @DisplayName("Should update person client with new values")
        void shouldUpdatePersonClient() {
            
            var existingPerson = createPersonEntity();
            var originalBirthdate = existingPerson.getBirthdate();

            var updateDTO = new ClientUpdateDTO(
                    "Updated Name",
                    "updated@example.com",
                    "+33699999999"
            );

            var updatedPerson = createPersonEntity();
            updatedPerson.setName("Updated Name");
            updatedPerson.setEmail("updated@example.com");
            updatedPerson.setPhone("+33699999999");

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(existingPerson));
            when(clientRepository.save(any(Person.class))).thenReturn(updatedPerson);

            
            var result = clientService.updateClient(TEST_CLIENT_ID, updateDTO);

            
            assertThat(result).isInstanceOf(PersonDTO.class);
            var personDTO = (PersonDTO) result;
            assertThat(personDTO.name()).isEqualTo("Updated Name");
            assertThat(personDTO.email()).isEqualTo("updated@example.com");
            assertThat(personDTO.phone()).isEqualTo("+33699999999");
            assertThat(personDTO.birthdate()).isEqualTo(originalBirthdate);

            verify(clientRepository).findById(TEST_CLIENT_ID);
            verify(clientRepository).save(any(Person.class));
        }

        @Test
        @Order(2)
        @DisplayName("Should update company client without changing immutable identifier")
        void shouldUpdateCompanyClientPreservingIdentifier() {
            
            var existingCompany = createCompanyEntity();
            var originalIdentifier = existingCompany.getCompanyIdentifier();

            var updateDTO = new ClientUpdateDTO(
                    "Updated Corp Name",
                    "newemail@corp.com",
                    "+33688888888"
            );

            var updatedCompany = createCompanyEntity();
            updatedCompany.setName("Updated Corp Name");
            updatedCompany.setEmail("newemail@corp.com");
            updatedCompany.setPhone("+33688888888");

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(existingCompany));
            when(clientRepository.save(any(Company.class))).thenReturn(updatedCompany);

            
            var result = clientService.updateClient(TEST_CLIENT_ID, updateDTO);

            
            var companyDTO = (CompanyDTO) result;
            assertThat(companyDTO.companyIdentifier()).isEqualTo(originalIdentifier);
        }

        @Test
        @Order(3)
        @DisplayName("Should throw exception when updating non-existent client")
        void shouldThrowExceptionWhenUpdatingNonExistentClient() {
            
            var updateDTO = new ClientUpdateDTO("Name", "email@test.com", "+33612345678");
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> clientService.updateClient(999L, updateDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found with id: 999");

            verify(clientRepository).findById(999L);
            verify(clientRepository, never()).save(any());
        }

        @Test
        @Order(4)
        @DisplayName("Should handle partial updates correctly")
        void shouldHandlePartialUpdatesCorrectly() {
            
            var existingPerson = createPersonEntity();
            var updateDTO = new ClientUpdateDTO(
                    "Only Name Changed",
                    existingPerson.getEmail(), // Same email
                    existingPerson.getPhone()  // Same phone
            );

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(existingPerson));
            when(clientRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

            
            clientService.updateClient(TEST_CLIENT_ID, updateDTO);

            
            verify(clientRepository).save(argThat(client ->
                    client.getName().equals("Only Name Changed") &&
                            client.getEmail().equals(existingPerson.getEmail())
            ));
        }
    }

    @Nested
    @DisplayName("Delete Client Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteClientTests {

        @Test
        @Order(1)
        @DisplayName("Should delete client and update active contract end dates")
        void shouldDeleteClientAndUpdateContractEndDates() {
            
            var client = createPersonEntity();
            var activeContract1 = createContract(1L, client, null); // No end date
            var activeContract2 = createContract(2L, client, LocalDate.now().plusMonths(6));
            var expiredContract = createContract(3L, client, LocalDate.now().minusDays(1));

            var contracts = List.of(activeContract1, activeContract2, expiredContract);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(client));
            when(contractRepository.findByClientId(TEST_CLIENT_ID)).thenReturn(contracts);
            doNothing().when(clientRepository).delete(client);

            
            clientService.deleteClient(TEST_CLIENT_ID);

            
            assertThat(activeContract1.getEndDate()).isEqualTo(LocalDate.now());
            assertThat(activeContract2.getEndDate()).isEqualTo(LocalDate.now());
            assertThat(expiredContract.getEndDate()).isEqualTo(LocalDate.now().minusDays(1)); // Unchanged

            verify(clientRepository).findById(TEST_CLIENT_ID);
            verify(contractRepository).findByClientId(TEST_CLIENT_ID);
            verify(clientRepository).delete(client);
        }

        @Test
        @Order(2)
        @DisplayName("Should delete client with no contracts")
        void shouldDeleteClientWithNoContracts() {
            
            var client = createPersonEntity();

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(client));
            when(contractRepository.findByClientId(TEST_CLIENT_ID)).thenReturn(Collections.emptyList());

            
            assertThatCode(() -> clientService.deleteClient(TEST_CLIENT_ID))
                    .doesNotThrowAnyException();

            
            verify(clientRepository).delete(client);
        }

        @Test
        @Order(3)
        @DisplayName("Should throw exception when deleting non-existent client")
        void shouldThrowExceptionWhenDeletingNonExistentClient() {
            
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> clientService.deleteClient(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found with id: 999");

            verify(clientRepository, never()).delete(any());
        }

        @Test
        @Order(4)
        @DisplayName("Should only update contracts with future or null end dates")
        void shouldOnlyUpdateActiveContracts() {
            
            var client = createPersonEntity();
            var futureContract = createContract(1L, client, LocalDate.now().plusYears(1));
            var pastContract = createContract(2L, client, LocalDate.now().minusYears(1));
            var todayContract = createContract(3L, client, LocalDate.now());

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(client));
            when(contractRepository.findByClientId(TEST_CLIENT_ID))
                    .thenReturn(List.of(futureContract, pastContract, todayContract));

            
            clientService.deleteClient(TEST_CLIENT_ID);

            
            assertThat(futureContract.getEndDate()).isEqualTo(LocalDate.now());
            assertThat(pastContract.getEndDate()).isEqualTo(LocalDate.now().minusYears(1));
            assertThat(todayContract.getEndDate()).isEqualTo(LocalDate.now());
        }
    }


}
