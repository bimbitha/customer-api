package com.example.customerapi;

import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.customerapi.dto.CustomerRequest;
import com.example.customerapi.entity.Customer;
import com.example.customerapi.repository.CustomerRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    /*
     * Set the JVM timezone before Spring/Flyway starts.
     * This avoids the Asia/Calcutta timezone problem seen earlier.
     */
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18.6")
                    .withDatabaseName("customer_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    /*
     * Tell Spring Boot and Flyway to use the PostgreSQL
     * container instead of your local customer_db.
     */
    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        String jdbcUrl =
                postgres.getJdbcUrl() + "?options=-c%20TimeZone=UTC";

        // Spring datasource
        registry.add(
                "spring.datasource.url",
                () -> jdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        // Flyway datasource
        registry.add(
                "spring.flyway.url",
                () -> jdbcUrl
        );

        registry.add(
                "spring.flyway.user",
                postgres::getUsername
        );

        registry.add(
                "spring.flyway.password",
                postgres::getPassword
        );
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @AfterEach
    void cleanup() {
        customerRepository.deleteAll();
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api/customers";
    }

    @Test
    void shouldCreateCustomer() {

        CustomerRequest request = new CustomerRequest();

        request.setName("Integration Test");
        request.setEmail("integration@example.com");
        request.setAge(35L);
        request.setPhoneNumber(9876543210L);
        request.setAddress("Bengaluru");
        request.setStatus("ACTIVE");
        request.setCustomerType("PREMIUM");

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        baseUrl(),
                        request,
                        String.class
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().contains("Integration Test")
        );
    }

    @Test
    void shouldGetCustomerById() {

        Customer customer = new Customer();

        customer.setName("Test Customer");
        customer.setEmail("test@example.com");
        customer.setAge(30L);
        customer.setPhoneNumber(9876500000L);
        customer.setAddress("Bengaluru");
        customer.setStatus("ACTIVE");
        customer.setCustomerType("REGULAR");

        Customer saved =
                customerRepository.save(customer);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        baseUrl() + "/" + saved.getId(),
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().contains("Test Customer")
        );
    }

    @Test
    void shouldReturn404ForUnknownCustomer() {

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        baseUrl() + "/999999",
                        String.class
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
    }

    @Test
    void shouldRejectInvalidCustomer() {

        CustomerRequest request =
                new CustomerRequest();

        request.setName("");
        request.setEmail("invalid-email");
        request.setAge(-10L);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        baseUrl(),
                        request,
                        String.class
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );
    }

    @Test
    void shouldUpdateCustomer() {

        Customer customer = new Customer();

        customer.setName("Before Update");
        customer.setEmail("before@example.com");
        customer.setAge(30L);
        customer.setPhoneNumber(9876500000L);
        customer.setAddress("Bengaluru");
        customer.setStatus("ACTIVE");
        customer.setCustomerType("REGULAR");

        Customer saved =
                customerRepository.save(customer);

        CustomerRequest request =
                new CustomerRequest();

        request.setName("After Update");
        request.setEmail("after@example.com");
        request.setAge(31L);
        request.setPhoneNumber(9876500001L);
        request.setAddress("Mysuru");
        request.setStatus("ACTIVE");
        request.setCustomerType("PREMIUM");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CustomerRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        baseUrl() + "/" + saved.getId(),
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().contains("After Update")
        );
    }

    @Test
    void shouldDeleteCustomer() {

        Customer customer = new Customer();

        customer.setName("Delete Test");
        customer.setEmail("delete@example.com");
        customer.setAge(40L);
        customer.setPhoneNumber(9876500002L);
        customer.setAddress("Bengaluru");
        customer.setStatus("ACTIVE");
        customer.setCustomerType("REGULAR");

        Customer saved =
                customerRepository.save(customer);

        ResponseEntity<Void> response =
                restTemplate.exchange(
                        baseUrl() + "/" + saved.getId(),
                        HttpMethod.DELETE,
                        null,
                        Void.class
                );

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        assertFalse(
                customerRepository
                        .findById(saved.getId())
                        .isPresent()
        );
    }
}