package com.barbearia.agendamento.config;

import com.barbearia.agendamento.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

    private final SpaLoginSuccessHandler spaLoginSuccessHandler;
    private final SpaLoginFailureHandler spaLoginFailureHandler;
    private final SpaLogoutSuccessHandler spaLogoutSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(SpaLoginSuccessHandler spaLoginSuccessHandler,
                         SpaLoginFailureHandler spaLoginFailureHandler,
                         SpaLogoutSuccessHandler spaLogoutSuccessHandler,
                         CustomOAuth2UserService customOAuth2UserService,
                         OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.spaLoginSuccessHandler = spaLoginSuccessHandler;
        this.spaLoginFailureHandler = spaLoginFailureHandler;
        this.spaLogoutSuccessHandler = spaLogoutSuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(java.util.List.of(
                    "http://localhost:4200",
                    "http://127.0.0.1:4200",
                    "http://168.138.147.219",
                    "http://168.138.147.219:80",
                    "http://168.138.147.219:8080",
                    "http://168.138.147.219.nip.io"
                ));
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(java.util.List.of("*"));
                config.setExposedHeaders(java.util.List.of("Set-Cookie"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);
                return config;
            }))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, 
                    "/api/setup/required",
                    "/api/servicos/publicos",
                    "/api/chat/auth-check").permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/setup/primeiro-admin",
                    "/api/clientes/cadastro",
                    "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/", "/home", "/index",
                    "/clientes/cadastro", "/clientes/login", "/error",
                    "/config/inicial",
                    "/css/**", "/js/**", "/images/**",
                    "/oauth2/authorization/**",
                    "/login/oauth2/code/**").permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/clientes/cadastrar",
                    "/config/primeiro-admin",
                    "/admin/pesquisar-clientes",
                    "/admin/selecionar-cliente",
                    "/barbeiros", "/admin",
                    "/login/barbeiro", "/login/admin").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/barbeiro/**").hasRole("BARBEIRO")
                .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers("/api/auth/completar-cadastro").authenticated()
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/barbeiro/**").hasRole("BARBEIRO")
                .requestMatchers("/cliente/**").hasAnyRole("CLIENTE", "ADMIN")
                .requestMatchers("/agendamentos/**", "/perfil/**", "/api/agendamentos/**").authenticated()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    LoggerFactory.getLogger(SecurityConfig.class).info(
                        "=== AUTH ENTRY POINT ===");
                    LoggerFactory.getLogger(SecurityConfig.class).info(
                        "URI: {} {}, Exception: {}, Message: {}",
                        request.getMethod(), request.getRequestURI(),
                        authException.getClass().getName(), authException.getMessage());
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"Usuário não autenticado. Faça login novamente.\"}");
                    } else {
                        response.sendRedirect("/clientes/login");
                    }
                }))
            .formLogin(form -> form
                .loginPage("/clientes/login")
                .loginProcessingUrl("/clientes/login")
                .successHandler(spaLoginSuccessHandler)
                .failureHandler(spaLoginFailureHandler)
                .permitAll())
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(spaLogoutSuccessHandler)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        var provider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new org.springframework.security.authentication.ProviderManager(provider);
    }
}
