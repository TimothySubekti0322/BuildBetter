package com.buildbetter.shared.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.buildbetter.shared.exception.JwtAuthenticationException;
import com.buildbetter.shared.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jws<Claims> jws = jwtUtil.validate(token);
                Claims c = jws.getPayload();

                // Mandatory claims
                String role = c.get("role", String.class);
                List<String> roles = role == null
                        ? List.of()
                        : List.of(role.toUpperCase());

                String userId = c.getSubject();
                JwtAuthentication auth = new JwtAuthentication(userId, roles, c);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException ex) {
                // ⬇️ bubble up with a *marker* that it's specifically "expired"
                throw new JwtAuthenticationException("EXPIRED", ex); // ← see §2
            } catch (JwtException ex) {
                throw new JwtAuthenticationException("INVALID", ex);
            }
        }
        chain.doFilter(req, res);
    }
}
