package com.lightbend.futures;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

class CustomerService {

    private CustomerRepository customerRepository;

    CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    CompletionStage<UUID> addCustomer(String firstName, String lastName, String address, String phoneNumber) {
        UUID customerId = UUID.randomUUID();
        return customerRepository.saveCustomer(new Customer(
            customerId,
            firstName,
            lastName,
            address,
            phoneNumber
        )).thenApply(aVoid -> customerId);
    }

    CompletionStage<Optional<String>> getCustomerFirstName(UUID customerId) {
        return customerRepository.getCustomer(customerId).thenApply(customer -> customer.map(Customer::getFirstName));
    }

    CompletionStage<Optional<String>> getCustomerLastName(UUID customerId) {
        return customerRepository.getCustomer(customerId).thenApply(customer -> customer.map(Customer::getLastName));
    }

    CompletionStage<Optional<String>> getCustomerAddress(UUID customerId) {
        return customerRepository.getCustomer(customerId).thenApply(customer -> customer.map(Customer::getAddress));
    }

    CompletionStage<Optional<String>> getCustomerPhoneNumber(UUID customerId) {
        return customerRepository.getCustomer(customerId).thenApply(customer -> customer.map(Customer::getPhoneNumber));
    }
}
