package com.hirelens.noteapp.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email requerido")
        String email,

        @NotBlank(message = "Password requerida")
        String password
) {
}
