package com.bank.qalab.service;

import com.bank.qalab.domain.Customer;
import com.bank.qalab.domain.CustomerStatus;
import com.bank.qalab.dto.CustomerDtos.CreateCustomerRequest;
import com.bank.qalab.dto.CustomerDtos.CustomerResponse;
import com.bank.qalab.dto.CustomerDtos.PatchCustomerRequest;
import com.bank.qalab.dto.CustomerDtos.UpdateCustomerRequest;
import com.bank.qalab.dto.PageResponse;
import com.bank.qalab.error.BusinessException;
import com.bank.qalab.error.ConflictException;
import com.bank.qalab.error.ForbiddenException;
import com.bank.qalab.error.NotFoundException;
import com.bank.qalab.repo.AccountRepository;
import com.bank.qalab.repo.CustomerRepository;
import com.bank.qalab.repo.UserRepository;
import com.bank.qalab.security.AppUserPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final UserRepository users;

    public CustomerService(CustomerRepository customers, AccountRepository accounts, UserRepository users) {
        this.customers = customers;
        this.accounts = accounts;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(CustomerStatus status, String q, Pageable pageable) {
        String pattern = "%" + (q == null ? "" : q.trim().toLowerCase()) + "%";
        return PageResponse.of(customers.search(status, pattern, pageable).map(Mapper::toCustomer));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id, AppUserPrincipal actor) {
        Access.requireOwnsCustomer(actor, id);
        return Mapper.toCustomer(find(id));
    }

    public CustomerResponse create(CreateCustomerRequest req) {
        customers.findByEmail(req.email()).ifPresent(x -> {
            throw new ConflictException("Email already used: " + req.email());
        });
        Customer c = Customer.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .phone(req.phone())
                .dateOfBirth(req.dateOfBirth())
                .addressLine(req.addressLine())
                .city(req.city())
                .country(req.country())
                .status(CustomerStatus.ACTIVE)
                .build();
        return Mapper.toCustomer(customers.save(c));
    }

    public CustomerResponse update(Long id, UpdateCustomerRequest req, AppUserPrincipal actor) {
        Access.requireOwnsCustomer(actor, id);
        Customer c = find(id);
        if (!c.getEmail().equalsIgnoreCase(req.email())) {
            customers.findByEmail(req.email()).ifPresent(x -> {
                throw new ConflictException("Email already used: " + req.email());
            });
        }
        c.setFirstName(req.firstName());
        c.setLastName(req.lastName());
        c.setEmail(req.email());
        c.setPhone(req.phone());
        c.setDateOfBirth(req.dateOfBirth());
        c.setAddressLine(req.addressLine());
        c.setCity(req.city());
        c.setCountry(req.country());
        return Mapper.toCustomer(customers.save(c));
    }

    public CustomerResponse patch(Long id, PatchCustomerRequest req, AppUserPrincipal actor) {
        Access.requireOwnsCustomer(actor, id);
        Customer c = find(id);
        if (req.firstName() != null) c.setFirstName(req.firstName());
        if (req.lastName() != null) c.setLastName(req.lastName());
        if (req.email() != null && !req.email().equalsIgnoreCase(c.getEmail())) {
            customers.findByEmail(req.email()).ifPresent(x -> {
                throw new ConflictException("Email already used: " + req.email());
            });
            c.setEmail(req.email());
        }
        if (req.phone() != null) c.setPhone(req.phone());
        if (req.dateOfBirth() != null) c.setDateOfBirth(req.dateOfBirth());
        if (req.addressLine() != null) c.setAddressLine(req.addressLine());
        if (req.city() != null) c.setCity(req.city());
        if (req.country() != null) c.setCountry(req.country());
        if (req.status() != null) {
            if (!actor.isAdmin()) {
                throw new ForbiddenException("Only ADMIN can change customer status.");
            }
            c.setStatus(parseStatus(req.status()));
        }
        return Mapper.toCustomer(customers.save(c));
    }

    public void delete(Long id) {
        Customer c = find(id);
        if (accounts.countByCustomerId(id) > 0) {
            throw new ConflictException("Customer has accounts; close them before deleting.");
        }
        if (users.existsByCustomerId(id)) {
            throw new ConflictException("Customer has a linked login and cannot be deleted.");
        }
        customers.delete(c);
    }

    private Customer find(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: id=" + id));
    }

    private CustomerStatus parseStatus(String s) {
        try {
            return CustomerStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status '" + s + "'. Use ACTIVE, INACTIVE or BLOCKED.");
        }
    }
}
