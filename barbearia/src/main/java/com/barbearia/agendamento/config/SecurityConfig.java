package com.barbearia.agendamento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                // Rotas públicas (GET)
                                                .requestMatchers(HttpMethod.GET,
                                                                "/", "/home", "/index",
                                                                "/clientes/cadastro", "/clientes/login",
                                                                "/config/inicial",
                                                                "/css/**", "/js/**", "/images/**")
                                                .permitAll()

                                                // Rotas públicas (POST)
                                                .requestMatchers(HttpMethod.POST,
                                                                "/clientes/cadastrar",
                                                                "/config/primeiro-admin",
                                                                "/admin/pesquisar-clientes",
                                                                "/admin/selecionar-cliente",
                                                                "/barbeiros", "/admin", // cadastro barbeiro/admin
                                                                "/login/barbeiro", "/login/admin" // login personalizado
                                                ).permitAll()

                                                // Rotas administrativas
                                                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                                                // Rotas para barbeiros
                                                .requestMatchers("/barbeiro/**", "/api/barbeiro/**").hasRole("BARBEIRO")

                                                // Rotas autenticadas (qualquer usuário logado)
                                                .requestMatchers(
                                                                "/agendamentos/**",
                                                                "/perfil/**",
                                                                "/api/agendamentos/**")
                                                .authenticated()

                                                // Todas as outras exigem autenticação
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/clientes/login") // tela de login personalizada
                                                .defaultSuccessUrl("/", true)
                                                .failureUrl("/clientes/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                                .logoutSuccessUrl("/?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers(
                                                                "/clientes/cadastrar",
                                                                "/config/primeiro-admin",
                                                                "/admin/pesquisar-clientes",
                                                                "/barbeiros", "/admin",
                                                                "/login/barbeiro", "/login/admin"));

                return http.build();
        }

    @Bean
    PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
