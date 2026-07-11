package com.bank.qalab.web.api;

import com.bank.qalab.domain.CustomerStatus;
import com.bank.qalab.dto.CustomerDtos.CreateCustomerRequest;
import com.bank.qalab.dto.CustomerDtos.CustomerResponse;
import com.bank.qalab.dto.CustomerDtos.PatchCustomerRequest;
import com.bank.qalab.dto.CustomerDtos.UpdateCustomerRequest;
import com.bank.qalab.dto.PageResponse;
import com.bank.qalab.security.AppUserPrincipal;
import com.bank.qalab.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "2. Customers", description = "Customer profiles. Listing/creating/deleting is ADMIN-only.")
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /** ADMIN: list/search customers. Try ?q=alice&status=ACTIVE&page=0&size=10&sort=lastName,asc */
    @GetMapping
    public PageResponse<CustomerResponse> list(@RequestParam(required = false) CustomerStatus status,
                                               @RequestParam(required = false) String q,
                                               @PageableDefault(size = 20) Pageable pageable) {
        return customerService.list(status, q, pageable);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        return customerService.get(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id,
                                   @Valid @RequestBody UpdateCustomerRequest request,
                                   @AuthenticationPrincipal AppUserPrincipal principal) {
        return customerService.update(id, request, principal);
    }

    @PatchMapping("/{id}")
    public CustomerResponse patch(@PathVariable Long id,
                                  @Valid @RequestBody PatchCustomerRequest request,
                                  @AuthenticationPrincipal AppUserPrincipal principal) {
        return customerService.patch(id, request, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }
}
