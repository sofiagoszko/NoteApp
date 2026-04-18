package com.hirelens.noteapp.controllers;
 
import com.hirelens.noteapp.dto.NoteDTO;
import com.hirelens.noteapp.models.Note;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.services.NoteService;
import com.hirelens.noteapp.services.UserService;
 
import jakarta.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.Optional;
 
@RestController
@RequestMapping("/api/users/{userId}/notes")
@CrossOrigin(origins = "http://localhost:3000")
public class NoteController {
 
    @Autowired
    private NoteService noteService;
    @Autowired
    private UserService userService;
 

    // GET /api/users/{userId}/notes
    @GetMapping
    public ResponseEntity<?> getNotes(@PathVariable Long userId, @RequestParam(required = false) Boolean active, @RequestHeader("X-User-Id") Long requesterId) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
 
        if (!canAccess(requesterId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        
        List<Note> notes;
        if (active != null) {
            notes = noteService.getActiveNotesByUser(userId, active);
        } else {
            notes = noteService.getNotesByUser(userId);
        }
 
        return ResponseEntity.ok(notes);
    }
 
    // GET /api/users/{userId}/notes/{noteId}
    @GetMapping("/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable Long userId, @PathVariable Long noteId, @RequestHeader("X-User-Id") Long requesterId) {
        Optional<Note> noteOpt = noteService.getNoteById(noteId);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nota no encontrada");
        }
 
        Note note = noteOpt.get();

        if (!canAccess(requesterId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
 
        return ResponseEntity.ok(note);
    }
 
    // POST /api/users/{userId}/notes
    @PostMapping
    public ResponseEntity<?> createNote(@PathVariable Long userId, @Valid @RequestBody NoteDTO noteDTO, @RequestHeader("X-User-Id") Long requesterId) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        if (!canAccess(requesterId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
 
        Note note = noteService.createNote(noteDTO, userOpt.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }
 
    // PUT /api/users/{userId}/notes/{noteId}
    @PutMapping("/{noteId}")
    public ResponseEntity<?> editNote(@PathVariable Long userId, @PathVariable Long noteId, @Valid @RequestBody NoteDTO noteDTO, @RequestHeader("X-User-Id") Long requesterId) {
        Optional<Note> noteOpt = noteService.getNoteById(noteId);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nota no encontrada");
        }
 
        if (!canAccess(requesterId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
 
        try {
            noteService.editNote(noteId, noteDTO);
            return ResponseEntity.ok("Nota actualizada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
 
    // PATCH /api/users/{userId}/notes/{noteId}/toggle-active
    // Archiva o desarchiva la nota
    @PatchMapping("/{noteId}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable Long userId, @PathVariable Long noteId, @RequestHeader("X-User-Id") Long requesterId) {
        Optional<Note> noteOpt = noteService.getNoteById(noteId);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nota no encontrada");
        }
 
        if (!canAccess(requesterId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
 
        try {
            noteService.editStatusNote(noteId);
            return ResponseEntity.ok("Estado de la nota actualizado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        
    }
 
    // DELETE /api/users/{userId}/notes/{noteId}
    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote( @PathVariable Long userId, @PathVariable Long noteId, @RequestHeader("X-User-Id") Long requesterId) {
        Optional<Note> noteOpt = noteService.getNoteById(noteId);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nota no encontrada");
        }
 
        if (!canAccess(requesterId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
 
        noteService.deleteNote(noteId);
        return ResponseEntity.noContent().build();
    }

    private boolean canAccess(Long requesterId, Long targetUserId) {
        if (requesterId.equals(targetUserId)){
            return true;
        } 
        return userService.isAdmin(requesterId);
    }
}