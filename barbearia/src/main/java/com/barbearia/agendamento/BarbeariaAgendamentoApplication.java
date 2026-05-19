package com.barbearia.agendamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BarbeariaAgendamentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BarbeariaAgendamentoApplication.class, args);
    }
}