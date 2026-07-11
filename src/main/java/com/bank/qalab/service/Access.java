package com.bank.qalab.service;

import com.bank.qalab.domain.Account;
import com.bank.qalab.error.BusinessException;
import com.bank.qalab.error.ForbiddenException;
import com.bank.qalab.security.AppUserPrincipal;

/** Small authorization helpers used across services (ownership + role checks). */
public final class Access {

    private Access() {}

    /** The linked customer id, or 422 if the caller is not a customer (e.g. the ADMIN user). */
    public static Long requireCustomer(AppUserPrincipal actor) {
        if (actor.getCustomerId() == null) {
            throw new BusinessException(
                    "This endpoint requires a customer login. The ADMIN user has no customer profile.");
        }
        return actor.getCustomerId();
    }

    public static void requireOwnsCustomer(AppUserPrincipal actor, Long customerId) {
        if (!actor.isAdmin()
                && (actor.getCustomerId() == null || !actor.getCustomerId().equals(customerId))) {
            throw new ForbiddenException("You can only access your own data.");
        }
    }

    public static void requireOwnsAccount(AppUserPrincipal actor, Account account) {
        if (actor.isAdmin()) return;
        Long owner = account.getCustomer() != null ? account.getCustomer().getId() : null;
        if (owner == null || !owner.equals(actor.getCustomerId())) {
            throw new ForbiddenException("You do not own account " + account.getAccountNumber() + ".");
        }
    }
}
