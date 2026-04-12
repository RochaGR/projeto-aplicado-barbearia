package com.barbearia.agendamento.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AuthRestController {

    private final AuthenticationManager authenticationManager;

    public AuthRestController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public record LoginRequest(String email, String password) {}

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        if (req == null || req.email() == null || req.email().isBlank() || req.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email e senha são obrigatórios."));
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email().trim(), req.password()));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("username", authentication.getName());
            body.put("roles", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                    .collect(Collectors.toList()));
            return ResponseEntity.ok(body);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou senha inválidos."));
        }
    }

    @GetMapping("/api/auth/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", authentication.getName());
        body.put("roles", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .collect(Collectors.toList()));
        return body;
    }
}
