package com.buildbetter.consultation.websocket.chat.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.buildbetter.consultation.model.Room;
import com.buildbetter.consultation.repository.RoomRepository;
import com.buildbetter.shared.util.JwtUtil; // ← your existing helper

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final RoomRepository roomRepo;

    @Override
    public boolean beforeHandshake(
            org.springframework.http.server.ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        /* 1. --- Extract & validate JWT ---------------------------------- */
        // Cast to ServletServerHttpRequest to access getServletRequest()
        if (!(request instanceof org.springframework.http.server.ServletServerHttpRequest) ||
                !(response instanceof org.springframework.http.server.ServletServerHttpResponse)) {
            log.warn("Request/Response not instance of ServletServerHttpRequest/ServletServerHttpResponse");
            return false;
        }
        jakarta.servlet.http.HttpServletRequest servletRequest = ((org.springframework.http.server.ServletServerHttpRequest) request)
                .getServletRequest();
        jakarta.servlet.http.HttpServletResponse servletResponse = ((org.springframework.http.server.ServletServerHttpResponse) response)
                .getServletResponse();

        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("ChatAuthHandshakeInterceptor: beforeHandshake - Missing JWT");
            servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        String token = authHeader.substring(7);

        UUID requesterId;
        try {
            Jws<Claims> jws = jwtUtil.validate(token); // still re-using your helper
            // token stores userId in "id" claim → get("id", String.class)
            String idClaim = jws.getPayload().get("id", String.class);
            requesterId = UUID.fromString(idClaim);
        } catch (ExpiredJwtException ex) {
            log.warn("ChatAuthHandshakeInterceptor: beforeHandshake - Expired JWT");
            servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("ChatAuthHandshakeInterceptor: beforeHandshake - Invalid JWT: {}", ex.getMessage());
            servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        /* 2. --- Extract roomId from the URL ----------------------------- */
        String path = ((org.springframework.http.server.ServletServerHttpRequest) request)
                .getServletRequest().getRequestURI();
        String idPart = path.substring(path.lastIndexOf('/') + 1);
        UUID roomId;
        try {
            roomId = UUID.fromString(idPart);
        } catch (IllegalArgumentException ex) {
            log.warn("ChatAuthHandshakeInterceptor: beforeHandshake - Invalid roomId in URL: {}", idPart);
            servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }

        /* 3. --- Check membership --------------------------------------- */
        Room room = roomRepo.findById(roomId).orElse(null);
        if (room == null ||
                !(requesterId.equals(room.getArchitectId()) ||
                        requesterId.equals(room.getUserId()))) {

            log.warn("User {} tried to access room {} without permission", requesterId, roomId);
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        /* 4. --- Check booking window ----------------------------------- */
        // Use ZonedDateTime to make the comparison timezone-aware.
        // It's best practice to specify the timezone your business operates in.
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Jakarta"));
        LocalDateTime roomStartTime = room.getStartTime();

        // Convert the room's start and end times to the same timezone for a correct
        // comparison.
        ZonedDateTime zonedStartTime = roomStartTime.atZone(ZoneId.of("Asia/Jakarta"));
        ZonedDateTime zonedEndTime = room.getEndTime().atZone(ZoneId.of("Asia/Jakarta"));

        if (now.isBefore(zonedStartTime)) {
            log.warn("User {} tried to access room {} before session start. Current time: {}, Start time: {}",
                    requesterId, roomId, now, zonedStartTime);
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        } else if (now.isAfter(zonedEndTime)) {
            log.warn("User {} tried to access room {} after booking window. Current time: {}, End time: {}",
                    requesterId, roomId, now, zonedEndTime);
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        /* 5. --- Stash useful info for the handler ---------------------- */
        attributes.put("userId", requesterId);
        attributes.put("roomId", roomId);
        return true; // handshake allowed
    }

    @Override
    public void afterHandshake(
            org.springframework.http.server.ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception ex) {
        /* no-op */
    }
}
