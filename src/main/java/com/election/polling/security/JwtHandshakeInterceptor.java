package com.election.polling.security;

import com.election.polling.service.UserDetailsServiceImpl;
import com.election.polling.utility.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                try {
                    String username = jwtUtil.extractUsername(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        attributes.put("user", userDetails);
                        return true; // authenticated successfully
                    } else {
                        sendError(response, "Invalid JWT token");
                    }
                } catch (Exception e) {
                    sendError(response, "JWT error: " + e.getMessage());
                }
            } else {
                sendError(response, "Missing or malformed Authorization header");
            }
        }

        return false;
    }

    private void sendError(ServerHttpResponse response, String message) {
        if (response instanceof ServletServerHttpResponse servletResponse) {
            HttpServletResponse httpServletResponse = servletResponse.getServletResponse();
            try {
                httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpServletResponse.setContentType("application/json");
                httpServletResponse.getWriter().write("{\"error\": \"" + message + "\"}");
                httpServletResponse.flushBuffer();
            } catch (IOException ex) {
                System.err.println("Error sending WebSocket error: " + ex.getMessage());
            }
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}
