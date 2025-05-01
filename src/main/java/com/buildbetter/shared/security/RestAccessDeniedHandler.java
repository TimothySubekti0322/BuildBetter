package com.buildbetter.shared.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.buildbetter.shared.exception.ForbiddenException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex) throws IOException {

        ForbiddenException fe = new ForbiddenException("You don't have permission to access this resource");

        writeError(response, fe.getCode(), fe.getName(), fe.getMessage());
    }

    private void writeError(HttpServletResponse response,
            int code, String status, String message) throws IOException {

        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(),
                Map.of("code", code, "status", status, "message", message));
    }
}
