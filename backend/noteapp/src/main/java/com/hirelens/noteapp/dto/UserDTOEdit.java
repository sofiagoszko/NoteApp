package com.hirelens.noteapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTOEdit {
    
    @NotBlank(message = "Nickname requerido")
    private String nickname;

    @NotBlank(message = "Email requerido")
    @Email(message = "Email inválido")
    private String email;
    
}
