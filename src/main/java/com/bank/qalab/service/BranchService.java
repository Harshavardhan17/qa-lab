package com.bank.qalab.service;

import com.bank.qalab.dto.BranchDtos.BranchResponse;
import com.bank.qalab.error.NotFoundException;
import com.bank.qalab.repo.BranchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BranchService {

    private final BranchRepository branches;

    public BranchService(BranchRepository branches) {
        this.branches = branches;
    }

    public List<BranchResponse> list() {
        return branches.findAll().stream().map(Mapper::toBranch).toList();
    }

    public BranchResponse get(Long id) {
        return branches.findById(id).map(Mapper::toBranch)
                .orElseThrow(() -> new NotFoundException("Branch not found: id=" + id));
    }
}
