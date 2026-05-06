package com.barbearia.agendamento;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BarbeariaAgendamentoApplication {

    public static void main(String[] args) {
        try {
            // Tenta carregar .env da pasta barbearia (relativo ao diretório de trabalho)
            Dotenv dotenv = Dotenv.configure()
                    .directory("./barbearia")
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
                System.out.println("Loaded env: " + entry.getKey() + "=" + (entry.getKey().contains("SECRET") ? "***" : entry.getValue()));
            });
        } catch (Exception e) {
            System.out.println("Dotenv not loaded: " + e.getMessage());
            // Tenta carregar do diretório atual
            try {
                Dotenv dotenv2 = Dotenv.configure()
                        .ignoreIfMissing()
                        .load();
                dotenv2.entries().forEach(entry -> {
                    System.setProperty(entry.getKey(), entry.getValue());
                    System.out.println("Loaded env (current dir): " + entry.getKey());
                });
            } catch (Exception e2) {
                System.out.println("Dotenv not loaded from current dir either.");
            }
        }
        // Debug: imprime se as variáveis foram carregadas
        System.out.println("GOOGLE_CLIENT_ID present: " + (System.getProperty("GOOGLE_CLIENT_ID") != null));
        System.out.println("GOOGLE_CLIENT_SECRET present: " + (System.getProperty("GOOGLE_CLIENT_SECRET") != null));
        SpringApplication.run(BarbeariaAgendamentoApplication.class, args);
    }
}