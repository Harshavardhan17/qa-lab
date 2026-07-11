package com.bank.qalab.security;

import com.bank.qalab.domain.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * The authenticated principal shared by both security chains (JWT API + form-login UI).
 * Carries the app user id, role and the linked customer id (null for ADMIN).
 */
public class AppUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String role;      // CUSTOMER / ADMIN
    private final Long customerId;   // null for admin users
    private final boolean enabled;

    public AppUserPrincipal(Long id, String username, String password,
                            String role, Long customerId, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.customerId = customerId;
        this.enabled = enabled;
    }

    public static AppUserPrincipal from(AppUser u) {
        Long cid = u.getCustomer() != null ? u.getCustomer().getId() : null;
        return new AppUserPrincipal(u.getId(), u.getUsername(), u.getPassword(),
                u.getRole().name(), cid, u.isEnabled());
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "ADMIN".equals(role); }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
