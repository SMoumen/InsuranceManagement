package com.insurance.integration;

import com.insurance.dto.*;
import com.insurance.models.Contract;
import com.insurance.models.Person;
import com.insurance.repository.ClientRepository;
import com.insurance.repository.ContractRepository;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Contract Performance Tests")
@Slf4j
class BenchmarkGetContractsTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("insurance_perf_test")
            .withUsername("perf_user")
            .withPassword("perf_password")
            .withReuse(true);

    @LocalServerPort
    private Integer port;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ContractRepository contractRepository;

    // Store client IDs mapped to number of contracts they have
    private static final Map<Integer, Long> clientIdsByContractCount = new HashMap<>();
    private static final Map<Integer, BigDecimal> expectedSums = new HashMap<>();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Performance optimizations
        registry.add("spring.jpa.properties.hibernate.jdbc.batch_size", () -> "100");
        registry.add("spring.jpa.properties.hibernate.order_inserts", () -> "true");
        registry.add("spring.jpa.properties.hibernate.order_updates", () -> "true");
        registry.add("spring.jpa.properties.hibernate.jdbc.batch_versioned_data", () -> "true");
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @BeforeAll
    static void setUpTestData(
            @Autowired ClientRepository clientRepository,
            @Autowired ContractRepository contractRepository) {


        int[] contractCounts = {5, 10, 100, 1000, 10000, 50000};

        for (int count : contractCounts) {
            long startTime = System.nanoTime();

            // Create client
            Person client = new Person();
            client.setName("Perf Test Client " + count);
            client.setEmail("perf" + count + "@example.com");
            client.setPhone("+33612345678");
            client.setBirthdate(LocalDate.of(1990, 1, 1));
            client = clientRepository.save(client);

            // Store client ID
            clientIdsByContractCount.put(count, client.getId());

            // Generate contracts in batches for better performance
            int batchSize = 1000;
            BigDecimal totalSum = BigDecimal.ZERO;

            for (int batch = 0; batch < count; batch += batchSize) {
                int batchEnd = Math.min(batch + batchSize, count);
                List<Contract> batchContracts = new ArrayList<>(batchEnd - batch);

                for (int i = batch; i < batchEnd; i++) {
                    Contract contract = new Contract();
                    contract.setClient(client);
                    contract.setStartDate(LocalDate.now().minusYears(i % 10));
                    contract.setEndDate(null); // Active contract
                    BigDecimal amount = BigDecimal.valueOf(1000 + (i % 100));
                    contract.setCostAmount(amount);

                    batchContracts.add(contract);
                    totalSum = totalSum.add(amount);
                }

                contractRepository.saveAll(batchContracts);

            }

            expectedSums.put(count, totalSum);

        }

        log.info("========================================");
        log.info("Test data setup complete!");
        log.info("Total clients created: {}", contractCounts.length);
        log.info("Total contracts created: {}",
                IntStream.of(contractCounts).sum());
        log.info("========================================");
    }

    /**
     * Parameterized test that uses pre-created data
     */
    @ParameterizedTest(name = "Should calculate sum for {0} contracts in reasonable time")
    @MethodSource("contractCountProvider")
    @Order(1)
    @DisplayName("Performance test: Sum calculation")
    void shouldCalculateSumWithGoodPerformance(int contractCount) {
        Long clientId = clientIdsByContractCount.get(contractCount);
        BigDecimal expectedSum = expectedSums.get(contractCount);

        assertThat(clientId).isNotNull();
        assertThat(expectedSum).isNotNull();

        log.info("Testing sum calculation for {} contracts (client ID: {})", contractCount, clientId);

        given()
                .when()
                .get("/api/contracts/client/{clientId}/sum", clientId)
                .then()
                .statusCode(200);


        List<Double> times = new ArrayList<>();
        int runs = contractCount >= 10000 ? 3 : 5;

        for (int i = 0; i < runs; i++) {
            long startTime = System.nanoTime();

            given()
                    .when()
                    .get("/api/contracts/client/{clientId}/sum", clientId)
                    .then()
                    .statusCode(200)
                    .body("totalCostAmount", equalTo(expectedSum.floatValue()));

            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;
            times.add(durationMs);
        }

        // Calculate statistics
        double avgTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double minTime = times.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxTime = times.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        log.info("Performance for {} contracts:", contractCount);
        log.info("  Min: {}ms", minTime);
        log.info("  Avg: {}ms", avgTime);
        log.info("  Max: {}ms", maxTime);
        log.info("  Expected sum: {}", expectedSum);

    }

    static Stream<Arguments> contractCountProvider() {
        return Stream.of(
                Arguments.of(5),
                Arguments.of(10),
                Arguments.of(100),
                Arguments.of(1000),
                Arguments.of(10000),
                Arguments.of(50000)
        );
    }

}