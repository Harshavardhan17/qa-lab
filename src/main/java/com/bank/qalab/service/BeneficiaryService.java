package com.bank.qalab.service;

import com.bank.qalab.domain.Beneficiary;
import com.bank.qalab.domain.Customer;
import com.bank.qalab.dto.BeneficiaryDtos.BeneficiaryRequest;
import com.bank.qalab.dto.BeneficiaryDtos.BeneficiaryResponse;
import com.bank.qalab.error.NotFoundException;
import com.bank.qalab.repo.BeneficiaryRepository;
import com.bank.qalab.repo.CustomerRepository;
import com.bank.qalab.security.AppUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaries;
    private final CustomerRepository customers;

    public BeneficiaryService(BeneficiaryRepository beneficiaries, CustomerRepository customers) {
        this.beneficiaries = beneficiaries;
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> list(AppUserPrincipal actor) {
        Long customerId = Access.requireCustomer(actor);
        return beneficiaries.findByCustomerIdOrderByNameAsc(customerId).stream().map(Mapper::toBeneficiary).toList();
    }

    public BeneficiaryResponse create(BeneficiaryRequest req, AppUserPrincipal actor) {
        Long customerId = Access.requireCustomer(actor);
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: id=" + customerId));
        Beneficiary b = Beneficiary.builder()
                .customer(c)
                .name(req.name())
                .nickname(req.nickname())
                .bankName(req.bankName())
                .accountNumber(req.accountNumber())
                .ifsc(req.ifsc())
                .build();
        return Mapper.toBeneficiary(beneficiaries.save(b));
    }

    @Transactional(readOnly = true)
    public BeneficiaryResponse get(Long id, AppUserPrincipal actor) {
        return Mapper.toBeneficiary(findOwned(id, actor));
    }

    public BeneficiaryResponse update(Long id, BeneficiaryRequest req, AppUserPrincipal actor) {
        Beneficiary b = findOwned(id, actor);
        b.setName(req.name());
        b.setNickname(req.nickname());
        b.setBankName(req.bankName());
        b.setAccountNumber(req.accountNumber());
        b.setIfsc(req.ifsc());
        return Mapper.toBeneficiary(beneficiaries.save(b));
    }

    public void delete(Long id, AppUserPrincipal actor) {
        beneficiaries.delete(findOwned(id, actor));
    }

    private Beneficiary findOwned(Long id, AppUserPrincipal actor) {
        Long customerId = Access.requireCustomer(actor);
        return beneficiaries.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new NotFoundException("Beneficiary not found: id=" + id));
    }
}
