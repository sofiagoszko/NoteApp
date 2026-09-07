package com.hirelens.noteapp.config;

import java.time.Duration;

import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(

        @NotBlank
        @Length(min = 32, message = "app.jwt.secret debe tener al menos 32 caracteres (clave HS256 de 256 bits)")
        String secret,

        @NotBlank
        String issuer,

        @NotNull
        Duration expiration
) {
}
