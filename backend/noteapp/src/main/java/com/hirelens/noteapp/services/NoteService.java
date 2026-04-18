package com.hirelens.noteapp.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hirelens.noteapp.dto.NoteDTO;
import com.hirelens.noteapp.models.Note;
import com.hirelens.noteapp.repositories.NoteRepository;

@Service
public class NoteService {
    @Autowired
    private NoteRepository noteRepository;

    public Note createNote(NoteDTO noteDTO) {
        Note note = new Note();
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        note.setCreatedAt(LocalDateTime.now());
        note.setActive(true);

        return noteRepository.save(note);
    }

    public void editNote(Long id, NoteDTO noteDTO) throws BadRequestException {
        Note existingNote = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));


        existingNote.setTitle(noteDTO.getTitle());
        existingNote.setContent(noteDTO.getContent());
        existingNote.setUpdatedAt(LocalDateTime.now());
        noteRepository.save(existingNote);
    }

    public void editStatusNote(Long id) {
        Note existingNote = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));

        existingNote.setActive(!existingNote.isActive());
        noteRepository.save(existingNote);
    }   

    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }

    public List<Note> getNotesByUser(Long userId) {
        return noteRepository.findByUserId(userId);   
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }
}
