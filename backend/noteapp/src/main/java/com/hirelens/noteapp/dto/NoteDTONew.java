package com.hirelens.noteapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteDTONew {

    @NotBlank(message = "Titulo requerido")
    private String title;
    
    @NotBlank(message = "Contenido requerido")
    @Size(max = 500, message = "El contenido no puede exceder los 500 caracteres")
    private String content; 

}
