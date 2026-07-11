package com.bank.qalab.dto;

public final class BranchDtos {

    private BranchDtos() {}

    public record BranchResponse(
            Long id,
            String name,
            String ifsc,
            String city,
            String address
    ) {}
}
