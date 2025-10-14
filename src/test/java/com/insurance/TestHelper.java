package com.insurance;

import com.insurance.dto.CompanyDTO;
import com.insurance.dto.PersonDTO;
import com.insurance.models.Client;
import com.insurance.models.Company;
import com.insurance.models.Contract;
import com.insurance.models.Person;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestHelper {

    public static final Long TEST_CLIENT_ID = 1L;
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PHONE = "+33612345678";
    public static PersonDTO createValidPersonDTO(Long id) {
        return new PersonDTO(
                id,
                "John Doe",
                TEST_EMAIL,
                TEST_PHONE,
                LocalDate.of(1990, 5, 15)
        );
    }

    public static CompanyDTO createValidCompanyDTO(Long id) {
        return new CompanyDTO(
                id,
                "Tech Corp",
                "contact@techcorp.com",
                "+33698765432",
                "abc-123"
        );
    }

    public static Person createPersonEntity() {
        var person = new Person();
        person.setId(TEST_CLIENT_ID);
        person.setName("John Doe");
        person.setEmail(TEST_EMAIL);
        person.setPhone(TEST_PHONE);
        person.setBirthdate(LocalDate.of(1990, 5, 15));
        return person;
    }

    public static Company createCompanyEntity() {
        var company = new Company();
        company.setId(TEST_CLIENT_ID);
        company.setName("Tech Corp");
        company.setEmail("contact@techcorp.com");
        company.setPhone("+33698765432");
        company.setCompanyIdentifier("abc-123");
        return company;
    }

    public static Contract createContract(Long id, Client client, LocalDate endDate) {
        var contract = new Contract();
        contract.setId(id);
        contract.setClient(client);
        contract.setStartDate(LocalDate.now().minusMonths(1));
        contract.setEndDate(endDate);
        contract.setCostAmount(new BigDecimal("1000.00"));
        return contract;
    }
}
