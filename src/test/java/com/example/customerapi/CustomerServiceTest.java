package com.example.customerapi;
import com.example.customerapi.service.CustomerService;

import com.example.customerapi.dto.CustomerRequest;
import com.example.customerapi.dto.CustomerResponse;
import com.example.customerapi.entity.Customer;
import com.example.customerapi.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void shouldGetCustomerById() {

        Customer customer = new Customer();

        customer.setId(1L);
        customer.setName("John");
        customer.setEmail("john@example.com");
        customer.setAge(35L);
        customer.setPhoneNumber(9876543210L);
        customer.setAddress("Bengaluru");
        customer.setStatus("ACTIVE");
        customer.setCustomerType("PREMIUM");

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        CustomerResponse response =
                customerService.getCustomerById(1L);

        assertEquals(1L, response.getId());
        assertEquals("John", response.getName());
        assertEquals("john@example.com", response.getEmail());

        verify(customerRepository).findById(1L);
    }

    @Test
    void shouldCreateCustomer() {

        CustomerRequest request = new CustomerRequest();

        request.setName("Priya");
        request.setEmail("priya@example.com");
        request.setAge(29L);
        request.setPhoneNumber(9876500000L);
        request.setAddress("Chennai");
        request.setStatus("ACTIVE");
        request.setCustomerType("REGULAR");

        Customer savedCustomer = new Customer();

        savedCustomer.setId(10L);
        savedCustomer.setName("Priya");
        savedCustomer.setEmail("priya@example.com");
        savedCustomer.setAge(29L);
        savedCustomer.setPhoneNumber(9876500000L);
        savedCustomer.setAddress("Chennai");
        savedCustomer.setStatus("ACTIVE");
        savedCustomer.setCustomerType("REGULAR");

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(savedCustomer);

        CustomerResponse response =
                customerService.createCustomer(request);

        assertEquals(10L, response.getId());
        assertEquals("Priya", response.getName());
        assertEquals("priya@example.com", response.getEmail());

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerDoesNotExist() {

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> customerService.getCustomerById(999L)
        );

        verify(customerRepository).findById(999L);
    }
}