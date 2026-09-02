package com.hirelens.noteapp.controllers;

import com.hirelens.noteapp.dto.NoteDTOEdit;
import com.hirelens.noteapp.dto.NoteDTONew;
import com.hirelens.noteapp.models.Note;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.services.NoteService;
import com.hirelens.noteapp.services.UserService;
import com.hirelens.noteapp.responses.Response;

import jakarta.validation.Valid;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;
    @Autowired
    private UserService userService;

    // GET /api/notes/users
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    // GET /api/notes/users/{userId}/active?active=true|false
    @GetMapping("/users/{userId}/active")
    @PreAuthorize("@authz.isSelfOrAdmin(#userId, authentication)")
    public ResponseEntity<?> getNotes(@PathVariable Long userId, @RequestParam(required = false) Boolean active) {
        if (userService.getUserById(userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Usuario no encontrado", null));
        }

        List<Note> notes = (active != null)
                ? noteService.getActiveNotesByUser(userId, active)
                : noteService.getNotesByUser(userId);

        return ResponseEntity.ok(notes);
    }

    // GET /api/notes/users/{userId}/notes/{noteId}
    @GetMapping("/users/{userId}/notes/{noteId}")
    @PreAuthorize("@authz.isSelfOrAdmin(#userId, authentication)")
    public ResponseEntity<?> getNoteById(@PathVariable Long userId, @PathVariable Long noteId) {
        Optional<Note> noteOpt = noteService.getNoteForUser(noteId, userId);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Nota no encontrada", null));
        }
 
        Note note = noteOpt.get();

        if (!canAccessNote(requesterId, userId, note)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response<>(false, "Acceso denegado", null));
        }
 
        return ResponseEntity.ok(note);
    }

    // POST /api/notes/users/{userId}/notes
    @PostMapping("/users/{userId}/notes")
    @PreAuthorize("@authz.isSelfOrAdmin(#userId, authentication)")
    public ResponseEntity<?> createNote(@PathVariable Long userId, @Valid @RequestBody NoteDTONew noteDTO) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Usuario no encontrado", null));
        }

        Note note = noteService.createNote(noteDTO, userOpt.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    // PUT /api/notes/users/{userId}/notes/{noteId}
    @PutMapping("/users/{userId}/notes/{noteId}")
    @PreAuthorize("@authz.isSelfOrAdmin(#userId, authentication)")
    public ResponseEntity<?> editNote(@PathVariable Long userId, @PathVariable Long noteId,
                                      @Valid @RequestBody NoteDTOEdit noteDTO) throws BadRequestException {
        if (noteService.getNoteForUser(noteId, userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Nota no encontrada", null));
        }
 
        if (!canAccessNote(requesterId, userId, noteOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response<>(false, "Acceso denegado", null));
        }
 
        try {
            noteService.editNote(noteId, noteDTO);
            return ResponseEntity.ok(new Response<>(true, "Nota actualizada", noteDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // PATCH /api/notes/users/{userId}/notes/{noteId}/toggle-active
    @PatchMapping("/users/{userId}/notes/{noteId}/toggle-active")
    @PreAuthorize("@authz.isSelfOrAdmin(#userId, authentication)")
    public ResponseEntity<?> toggleActive(@PathVariable Long userId, @PathVariable Long noteId) throws BadRequestException {
        Optional<Note> noteOpt = noteService.getNoteForUser(noteId, userId);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Nota no encontrada", null));
        }
 
        if (!canAccessNote(requesterId, userId, noteOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response<>(false, "Acceso denegado", null));
        }
 
        try {
            noteService.editStatusNote(noteId);
            return ResponseEntity.ok(new Response<>(true, "estado de la nota actualizado", noteOpt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        
    }

    // DELETE /api/notes/users/{userId}/notes/{noteId}
    @DeleteMapping("/users/{userId}/notes/{noteId}")
    @PreAuthorize("@authz.isSelfOrAdmin(#userId, authentication)")
    public ResponseEntity<?> deleteNote(@PathVariable Long userId, @PathVariable Long noteId) {
        Optional<Note> noteOpt = noteService.getNoteForUser(noteId, userId);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Nota no encontrada", null));
        }
 
        if (!canAccessNote(requesterId, userId, noteOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response<>(false, "Acceso denegado", null));
        }
 
        noteService.deleteNote(noteId);
        return ResponseEntity.ok(new Response<>(true, "Nota eliminada", noteOpt));
    }

    private boolean canAccess(Long requesterId, Long targetUserId) {
        if (requesterId.equals(targetUserId)){
            return true;
        } 
        return userService.isAdmin(requesterId);
    }

    private boolean canAccessNote(Long requesterId, Long targetUserId, Note note) {
        if (userService.isAdmin(requesterId)) {
            return true;
        }

        return requesterId.equals(targetUserId)
                && note.getUser() != null
                && note.getUser().getId().equals(targetUserId);
    }
}
