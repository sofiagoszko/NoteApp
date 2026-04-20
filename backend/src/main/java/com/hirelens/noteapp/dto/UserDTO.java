package com.hirelens.noteapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import com.hirelens.noteapp.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    
    @NotBlank(message = "Nickname requerido")
    private String nickname;

    @NotBlank(message = "Email requerido")
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "Password requerida")
    private String password;
    @NotBlank(message = "Confirmación de password requerida")
    private String passwordConfirm;

    private Role role;
}
