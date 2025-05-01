package com.buildbetter.shared.security;

import com.buildbetter.shared.exception.JwtAuthenticationException;
import com.buildbetter.shared.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Map;

public class RestAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest  request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        // default → invalid token
        UnauthorizedException ue =
            new UnauthorizedException("Invalid or missing JWT");

        if (ex instanceof JwtAuthenticationException jex &&
            jex.getReason() == JwtAuthenticationException.Reason.EXPIRED) {

            ue = new UnauthorizedException("Token is Expired");
        }

        writeError(response, ue.getCode(), ue.getName(), ue.getMessage());
    }

    private void writeError(HttpServletResponse response,
                            int code, String status, String message) throws IOException {

        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(),
                Map.of("code", code, "status", status, "message", message));
    }
}
