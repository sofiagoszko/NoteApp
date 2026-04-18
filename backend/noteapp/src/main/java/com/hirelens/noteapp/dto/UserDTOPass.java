package com.hirelens.noteapp.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTOPass {
    
    @NotBlank(message = "Password requerida")
    private String password;
    @NotBlank(message = "Confirmación de password requerida")
    private String passwordConfirm;

}
