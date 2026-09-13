package com.example.customerapi.service;

import com.example.customerapi.dto.CustomerRequest;
import com.example.customerapi.dto.CustomerResponse;
import com.example.customerapi.entity.Customer;
import com.example.customerapi.exception.CustomerNotFoundException;
import com.example.customerapi.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return toResponse(customer);
    }

    public CustomerResponse createCustomer(CustomerRequest request) {

        Customer customer = new Customer();

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setAge(request.getAge());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setStatus(request.getStatus());
        customer.setCustomerType(request.getCustomerType());

        Customer savedCustomer = customerRepository.save(customer);

        return toResponse(savedCustomer);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        existingCustomer.setName(request.getName());
        existingCustomer.setEmail(request.getEmail());
        existingCustomer.setAge(request.getAge());
        existingCustomer.setPhoneNumber(request.getPhoneNumber());
        existingCustomer.setAddress(request.getAddress());
        existingCustomer.setStatus(request.getStatus());
        existingCustomer.setCustomerType(request.getCustomerType());

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        return toResponse(updatedCustomer);
    }

    public void createCustomers(List<CustomerRequest> requests) {

        List<Customer> customers = requests.stream()
                .map(request -> {

                    Customer customer = new Customer();

                    customer.setName(request.getName());
                    customer.setEmail(request.getEmail());
                    customer.setAge(request.getAge());
                    customer.setPhoneNumber(request.getPhoneNumber());
                    customer.setAddress(request.getAddress());
                    customer.setStatus(request.getStatus());
                    customer.setCustomerType(request.getCustomerType());

                    return customer;

                })
                .toList();

        customerRepository.saveAll(customers);
    }

    public void deleteCustomer(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customerRepository.delete(customer);
    }

    private CustomerResponse toResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getAge(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getStatus(),
                customer.getCustomerType());
    }
}