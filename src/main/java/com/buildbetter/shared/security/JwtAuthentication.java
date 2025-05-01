package com.buildbetter.shared.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtAuthentication implements Authentication {

    private final String principal;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> claims;
    private boolean authenticated = true;

    public JwtAuthentication(String principal, List<String> roles, Map<String, Object> claims) {
        this.principal = principal;
        this.claims = claims;
        this.authorities = (roles == null ? List.<String>of() : roles)
                .stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return claims;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuth) throws IllegalArgumentException {
        this.authenticated = isAuth;
    }

    @Override
    public String getName() {
        return principal;
    }

    @SuppressWarnings("unchecked")
    public <T> T claim(String key) {
        return (T) claims.get(key);
    }
}
