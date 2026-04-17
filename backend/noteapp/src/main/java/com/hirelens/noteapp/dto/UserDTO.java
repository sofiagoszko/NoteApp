package com.hirelens.noteapp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String nickname;
    private String email;
    private String password;
    private List<NoteDTO> notes;
}
