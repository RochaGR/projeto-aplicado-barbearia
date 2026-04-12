package com.barbearia.agendamento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
<<<<<<< HEAD
=======
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
>>>>>>> 3986e7d

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final SpaLoginSuccessHandler spaLoginSuccessHandler;
        private final SpaLoginFailureHandler spaLoginFailureHandler;
        private final SpaLogoutSuccessHandler spaLogoutSuccessHandler;

        public SecurityConfig(SpaLoginSuccessHandler spaLoginSuccessHandler,
                        SpaLoginFailureHandler spaLoginFailureHandler,
                        SpaLogoutSuccessHandler spaLogoutSuccessHandler) {
                this.spaLoginSuccessHandler = spaLoginSuccessHandler;
                this.spaLoginFailureHandler = spaLoginFailureHandler;
                this.spaLogoutSuccessHandler = spaLogoutSuccessHandler;
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                requestHandler.setCsrfRequestAttributeName("_csrf");

                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(requestHandler)
                                                .ignoringRequestMatchers("/clientes/login", "/logout",
                                                                "/api/auth/login"))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/setup/required",
                                                                "/api/servicos/publicos")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/setup/primeiro-admin",
                                                                "/api/clientes/cadastro",
                                                                "/api/auth/login")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/", "/home", "/index",
                                                                "/clientes/cadastro", "/clientes/login",
                                                                "/config/inicial",
                                                                "/css/**", "/js/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST,
                                                                "/clientes/cadastrar",
                                                                "/config/primeiro-admin",
                                                                "/admin/pesquisar-clientes",
                                                                "/admin/selecionar-cliente",
                                                                "/barbeiros", "/admin",
                                                                "/login/barbeiro", "/login/admin")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/barbeiro/**").hasRole("BARBEIRO")
                                                .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                                                .requestMatchers("/api/auth/me").authenticated()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/barbeiro/**").hasRole("BARBEIRO")
                                                .requestMatchers("/cliente/**").hasAnyRole("CLIENTE", "ADMIN")
                                                .requestMatchers(
                                                                "/agendamentos/**",
                                                                "/perfil/**",
                                                                "/api/agendamentos/**")
                                                .authenticated()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/clientes/login")
                                                .loginProcessingUrl("/clientes/login")
                                                .successHandler(spaLoginSuccessHandler)
                                                .failureHandler(spaLoginFailureHandler)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessHandler(spaLogoutSuccessHandler)
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());

<<<<<<< HEAD
                        // Rotas para clientes
                        .requestMatchers("/cliente/fidelidade").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers("/cliente/**").hasAnyRole("CLIENTE", "ADMIN")
=======
                return http.build();
        }
>>>>>>> 3986e7d

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

<<<<<<< HEAD
                        // Todas as outras exigem autenticação
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/clientes/login") // tela de login personalizada
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/clientes/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
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
=======
        @Bean
        AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }
}
>>>>>>> 3986e7d
