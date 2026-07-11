package com.bank.qalab.web.api;

import com.bank.qalab.dto.BranchDtos.BranchResponse;
import com.bank.qalab.service.BranchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "9. Branches", description = "Public reference data (no authentication required)")
@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public List<BranchResponse> list() {
        return branchService.list();
    }

    @GetMapping("/{id}")
    public BranchResponse get(@PathVariable Long id) {
        return branchService.get(id);
    }
}
