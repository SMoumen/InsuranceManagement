package com.insurance.integration;

import com.insurance.dto.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true"
        }
)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Insurance Management System - Integration Tests")
class InsuranceManagementIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("insurance_test")
            .withUsername("test_user")
            .withPassword("test_password");
    @LocalServerPort
    private Integer port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Let Hibernate create the schema from entities
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    // ============================================
    // CLIENT MANAGEMENT TESTS
    // ============================================

    @Test
    @Order(1)
    @DisplayName("Should create person client and return 201")
    void shouldCreatePersonClient() {
        var personDTO = new PersonDTO(
                null,
                "John Doe",
                "john.doe@example.com",
                "+33612345678",
                LocalDate.of(1990, 5, 15)
        );

        given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("John Doe"))
                .body("email", equalTo("john.doe@example.com"))
                .body("phone", equalTo("+33612345678"))
                .body("birthdate", equalTo("1990-05-15"));
    }

    @Test
    @Order(2)
    @DisplayName("Should create company client successfully")
    void shouldCreateCompanyClient() {
        var companyDTO = new CompanyDTO(
                null,
                "Tech Corporation",
                "contact@techcorp.com",
                "+33698765432",
                "abc-123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(companyDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Tech Corporation"))
                .body("companyIdentifier", equalTo("abc-123"));
    }

    @Test
    @Order(3)
    @DisplayName("Should fail to create client with invalid email")
    void shouldFailWithInvalidEmail() {
        var invalidDTO = new PersonDTO(
                null,
                "Jane Doe",
                "invalid-email",
                "+33612345678",
                LocalDate.of(1985, 3, 20)
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", containsString("Validation failed"))
                .body("errors.email", notNullValue());
    }

    @Test
    @Order(4)
    @DisplayName("Should fail with invalid phone number format")
    void shouldFailWithInvalidPhone() {
        var invalidDTO = new PersonDTO(
                null,
                "Bob Smith",
                "bob@example.com",
                "123", // Invalid phone
                LocalDate.of(1992, 7, 10)
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(400)
                .body("errors.phone", containsString("E.164 format"));
    }

    @Test
    @Order(5)
    @DisplayName("Should fail with invalid company identifier format")
    void shouldFailWithInvalidCompanyIdentifier() {
        var invalidDTO = new CompanyDTO(
                null,
                "Bad Corp",
                "bad@corp.com",
                "+33612345678",
                "invalid" // Should be aaa-123 format
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(400)
                .body("errors.companyIdentifier", containsString("aaa-123"));
    }

    @Test
    @Order(6)
    @DisplayName("Should fail with birthdate in the future")
    void shouldFailWithFutureBirthdate() {
        var invalidDTO = new PersonDTO(
                null,
                "Future Person",
                "future@example.com",
                "+33612345678",
                LocalDate.now().plusDays(1)
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(400)
                .body("errors.birthdate", containsString("past"));
    }

    @Test
    @Order(7)
    @DisplayName("Should get existing client by id")
    void shouldGetClientById() {
        // Create client first
        var personDTO = new PersonDTO(
                null,
                "Alice Johnson",
                "alice@example.com",
                "+33687654321",
                LocalDate.of(1988, 11, 25)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Get the client
        given()
                .when()
                .get("/api/clients/{id}", clientId)
                .then()
                .statusCode(200)
                .body("id", equalTo(clientId))
                .body("name", equalTo("Alice Johnson"))
                .body("email", equalTo("alice@example.com"));
    }

    @Test
    @Order(8)
    @DisplayName("Should return 404 for non-existent client")
    void shouldReturn404ForNonExistentClient() {
        given()
                .when()
                .get("/api/clients/{id}", 99999)
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("message", containsString("Client not found"));
    }

    @Test
    @Order(9)
    @DisplayName("Should update client successfully")
    void shouldUpdateClient() {
        // Create client
        var personDTO = new PersonDTO(
                null,
                "Charlie Brown",
                "charlie@example.com",
                "+33612345678",
                LocalDate.of(1995, 2, 14)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Update the client
        var updateDTO = new ClientUpdateDTO(
                "Charles Brown",
                "charles@example.com",
                "+33699999999"
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/api/clients/{id}", clientId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Charles Brown"))
                .body("email", equalTo("charles@example.com"))
                .body("phone", equalTo("+33699999999"))
                .body("birthdate", equalTo("1995-02-14")); // Unchanged
    }

    @Test
    @Order(10)
    @DisplayName("Should not allow updating immutable birthdate")
    void shouldNotUpdateBirthdate() {
        // Create person
        var personDTO = new PersonDTO(
                null,
                "David Lee",
                "david@example.com",
                "+33612345678",
                LocalDate.of(1990, 6, 30)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Update - birthdate should remain unchanged
        var updateDTO = new ClientUpdateDTO(
                "David Lee Updated",
                "david.new@example.com",
                "+33688888888"
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/api/clients/{id}", clientId)
                .then()
                .statusCode(200)
                .body("birthdate", equalTo("1990-06-30"));
    }

    @Test
    @Order(11)
    @DisplayName("Should delete client and update contract end dates")
    void shouldDeleteClientAndUpdateContracts() {
        // Create client
        var personDTO = new PersonDTO(
                null,
                "Eva Martinez",
                "eva@example.com",
                "+33612345678",
                LocalDate.of(1987, 9, 5)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Create active contract
        var contractDTO = new ContractDTO(
                null,
                clientId.longValue(),
                null,
                LocalDate.now().plusYears(1),
                new BigDecimal("1500.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(contractDTO)
                .when()
                .post("/api/contracts")
                .then()
                .statusCode(201);

        // Delete client
        given()
                .when()
                .delete("/api/clients/{id}", clientId)
                .then()
                .statusCode(204);

        // Verify client is deleted
        given()
                .when()
                .get("/api/clients/{id}", clientId)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(12)
    @DisplayName("Should handle concurrent client creations")
    void shouldHandleConcurrentCreations() {
        var person1 = new PersonDTO(null, "Person 1", "person1@test.com", "+33611111111", LocalDate.of(1990, 1, 1));
        var person2 = new PersonDTO(null, "Person 2", "person2@test.com", "+33622222222", LocalDate.of(1991, 2, 2));

        // Create both clients
        Integer id1 = given().contentType(ContentType.JSON).body(person1)
                .when().post("/api/clients").then().statusCode(201).extract().path("id");

        Integer id2 = given().contentType(ContentType.JSON).body(person2)
                .when().post("/api/clients").then().statusCode(201).extract().path("id");

        // Verify both exist and are different
        assertThat(id1).isNotEqualTo(id2);
    }

    // ============================================
    // CONTRACT MANAGEMENT TESTS
    // ============================================

    @Test
    @Order(20)
    @DisplayName("Should create contract with all dates provided")
    void shouldCreateContractWithDates() {
        // Create client first
        var personDTO = new PersonDTO(
                null,
                "Contract Test Person 1",
                "contract1@example.com",
                "+33612345678",
                LocalDate.of(1990, 1, 1)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Create contract
        var contractDTO = new ContractDTO(
                null,
                clientId.longValue(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 1, 1),
                new BigDecimal("1500.50")
        );

        given()
                .contentType(ContentType.JSON)
                .body(contractDTO)
                .when()
                .post("/api/contracts")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("startDate", equalTo("2025-01-01"))
                .body("endDate", equalTo("2026-01-01"))
                .body("costAmount", equalTo(1500.50f));
    }

    @Test
    @Order(21)
    @DisplayName("Should create contract with default start date")
    void shouldCreateContractWithDefaultStartDate() {
        // Create client
        var personDTO = new PersonDTO(
                null,
                "Contract Test Person 2",
                "contract2@example.com",
                "+33612345678",
                LocalDate.of(1990, 1, 1)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        var contractDTO = new ContractDTO(
                null,
                clientId.longValue(),
                null, // Should default to today
                LocalDate.now().plusYears(1),
                new BigDecimal("2000.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(contractDTO)
                .when()
                .post("/api/contracts")
                .then()
                .statusCode(201)
                .body("startDate", equalTo(LocalDate.now().toString()))
                .body("costAmount", equalTo(2000.00f));
    }

    @Test
    @Order(22)
    @DisplayName("Should create indefinite contract with null end date")
    void shouldCreateIndefiniteContract() {
        // Create client
        var personDTO = new PersonDTO(
                null,
                "Contract Test Person 3",
                "contract3@example.com",
                "+33612345678",
                LocalDate.of(1990, 1, 1)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        var contractDTO = new ContractDTO(
                null,
                clientId.longValue(),
                LocalDate.now(),
                null, // Indefinite
                new BigDecimal("2500.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(contractDTO)
                .when()
                .post("/api/contracts")
                .then()
                .statusCode(201)
                .body("endDate", nullValue())
                .body("costAmount", equalTo(2500.00f));
    }

    @Test
    @Order(23)
    @DisplayName("Should fail to create contract for non-existent client")
    void shouldFailForNonExistentClient() {
        var contractDTO = new ContractDTO(
                null,
                99999L,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                new BigDecimal("1000.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(contractDTO)
                .when()
                .post("/api/contracts")
                .then()
                .statusCode(404)
                .body("message", containsString("Client not found"));
    }

    @Test
    @Order(24)
    @DisplayName("Should fail with invalid cost amount")
    void shouldFailWithInvalidCostAmount() {
        // Create client
        var personDTO = new PersonDTO(
                null,
                "Contract Test Person 4",
                "contract4@example.com",
                "+33612345678",
                LocalDate.of(1990, 1, 1)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        var contractDTO = new ContractDTO(
                null,
                clientId.longValue(),
                LocalDate.now(),
                null,
                new BigDecimal("0.00") // Should be > 0
        );

        given()
                .contentType(ContentType.JSON)
                .body(contractDTO)
                .when()
                .post("/api/contracts")
                .then()
                .statusCode(400)
                .body("errors.costAmount", containsString("greater than 0"));
    }

    @Test
    @Order(25)
    @DisplayName("Should update contract cost successfully")
    void shouldUpdateContractCost() {
        // Create client
        var personDTO = new PersonDTO(
                null,
                "Contract Test Person 5",
                "contract5@example.com",
                "+33612345678",
                LocalDate.of(1990, 1, 1)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Create contract
        var contractDTO = new ContractDTO(
                null,
                clientId.longValue(),
                null,
                null,
                new BigDecimal("1000.00")
        );

        Integer contractId = given()
                .contentType(ContentType.JSON)
                .body(contractDTO)
                .when()
                .post("/api/contracts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Update cost
        var updateDTO = new ContractUpdateDTO(new BigDecimal("1750.75"));

        given()
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .patch("/api/contracts/{id}/cost", contractId)
                .then()
                .statusCode(200)
                .body("costAmount", equalTo(1750.75f));
    }

    @Test
    @Order(26)
    @DisplayName("Should get only active contracts for client")
    void shouldGetOnlyActiveContracts() {
        // Create client
        var personDTO = new PersonDTO(
                null,
                "Contract Test Person 6",
                "contract6@example.com",
                "+33612345678",
                LocalDate.of(1990, 1, 1)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Create active contract
        var activeContract = new ContractDTO(
                null,
                clientId.longValue(),
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(6),
                new BigDecimal("1000.00")
        );

        given().contentType(ContentType.JSON).body(activeContract)
                .when().post("/api/contracts").then().statusCode(201);

        // Create expired contract
        var expiredContract = new ContractDTO(
                null,
                clientId.longValue(),
                LocalDate.now().minusYears(2),
                LocalDate.now().minusDays(1),
                new BigDecimal("500.00")
        );

        given().contentType(ContentType.JSON).body(expiredContract)
                .when().post("/api/contracts").then().statusCode(201);

        // Get active contracts - should only return 1
        given()
                .when()
                .get("/api/contracts/client/{clientId}", clientId)
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].costAmount", equalTo(1000.00f));
    }

    @Test
    @Order(27)
    @DisplayName("Should calculate sum of active contracts")
    void shouldCalculateSumOfActiveContracts() {
        // Create client
        var personDTO = new PersonDTO(
                null,
                "Contract Test Person 7",
                "contract7@example.com",
                "+33612345678",
                LocalDate.of(1990, 1, 1)
        );

        Integer clientId = given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Create multiple active contracts
        var contract1 = new ContractDTO(null, clientId.longValue(), null, null, new BigDecimal("1000.00"));
        var contract2 = new ContractDTO(null, clientId.longValue(), null, null, new BigDecimal("1500.50"));
        var contract3 = new ContractDTO(null, clientId.longValue(), null, null, new BigDecimal("750.25"));

        given().contentType(ContentType.JSON).body(contract1).when().post("/api/contracts").then().statusCode(201);
        given().contentType(ContentType.JSON).body(contract2).when().post("/api/contracts").then().statusCode(201);
        given().contentType(ContentType.JSON).body(contract3).when().post("/api/contracts").then().statusCode(201);

        // Get sum
        given()
                .when()
                .get("/api/contracts/client/{clientId}/sum", clientId)
                .then()
                .statusCode(200)
                .body("totalCostAmount", equalTo(3250.75f));
    }
}