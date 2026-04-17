package com.hirelens.noteapp.mappers;

import com.hirelens.noteapp.dto.NoteDTO;
import com.hirelens.noteapp.models.Note;

public class NoteMapper {

    public NoteMapper() {
    }   

    public NoteDTO toDTO(Note note) {
        if (note == null) {
            return null;
        }
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setTitle(note.getTitle());
        noteDTO.setContent(note.getContent());
        noteDTO.setCreatedAt(note.getCreatedAt());
        noteDTO.setUpdatedAt(note.getUpdatedAt());
        noteDTO.setActive(note.isActive());

        return noteDTO;
    }

    public Note toEntity(NoteDTO noteDTO) {
        if (noteDTO == null) {
            return null;
        }
        Note note = new Note();
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        note.setCreatedAt(noteDTO.getCreatedAt());
        note.setUpdatedAt(noteDTO.getUpdatedAt());
        note.setActive(noteDTO.isActive());

        return note;
    }
}
