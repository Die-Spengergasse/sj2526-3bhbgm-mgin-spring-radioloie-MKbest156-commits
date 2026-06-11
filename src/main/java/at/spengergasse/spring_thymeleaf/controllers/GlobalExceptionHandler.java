package at.spengergasse.spring_thymeleaf.controllers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {

        model.addAttribute(
                "errorMessage",
                "Datenbankfehler: Bitte prüfen Sie, ob der MySQL-Server läuft."
        );

        return "error";
    }
}