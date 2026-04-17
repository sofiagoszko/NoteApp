package com.hirelens.noteapp.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteDTO {
    private String title;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private boolean active;
}
