package com.barbearia.agendamento.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class SpaLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpaLoginFailureHandler() {
        setDefaultFailureUrl("/clientes/login?error=true");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        if (!"true".equalsIgnoreCase(request.getHeader("X-SPA-Login"))) {
            super.onAuthenticationFailure(request, response, exception);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Email ou senha inválidos.");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
