package com.barbearia.agendamento.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaValidator implements ConstraintValidator<SenhaValida, String> {

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {

        if (senha == null || senha.isBlank()) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (senha.length() < 8) {
            context.buildConstraintViolationWithTemplate(
                            "A senha deve ter no mínimo 8 caracteres")
                    .addConstraintViolation();
            return false;
        }

        if (!senha.matches(".*[A-Z].*")) {
            context.buildConstraintViolationWithTemplate(
                            "A senha deve conter pelo menos 1 letra maiúscula")
                    .addConstraintViolation();
            return false;
        }

        if (!senha.matches(".*[a-z].*")) {
            context.buildConstraintViolationWithTemplate(
                            "A senha deve conter pelo menos 1 letra minúscula")
                    .addConstraintViolation();
            return false;
        }

        if (!senha.matches(".*[^a-zA-Z0-9].*")) {
            context.buildConstraintViolationWithTemplate(
                            "A senha deve conter pelo menos 1 caractere especial")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}