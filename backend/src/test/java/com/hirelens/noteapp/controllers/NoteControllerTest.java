package com.hirelens.noteapp.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hirelens.noteapp.config.SecurityConfig;
import com.hirelens.noteapp.dto.NoteDTOEdit;
import com.hirelens.noteapp.dto.NoteDTONew;
import com.hirelens.noteapp.enums.Role;
import com.hirelens.noteapp.models.Note;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.services.NoteService;
import com.hirelens.noteapp.services.UserService;

@WebMvcTest(NoteController.class)
@Import(SecurityConfig.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private UserService userService;

    @Test
    void getAllNotesReturnsNotesForAdmin() throws Exception {
        Note note = new Note(10L, "Titulo", "Contenido", null, null, true, null);

        when(userService.isAdmin(99L)).thenReturn(true);
        when(noteService.getAllNotes()).thenReturn(List.of(note));

        mockMvc.perform(get("/api/notes/users")
                        .header("X-User-Id", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].title").value("Titulo"))
                .andExpect(jsonPath("$[0].content").value("Contenido"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(noteService).getAllNotes();
    }

    @Test
    void getAllNotesReturnsForbiddenForNonAdmin() throws Exception {
        when(userService.isAdmin(1L)).thenReturn(false);

        mockMvc.perform(get("/api/notes/users")
                        .header("X-User-Id", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));

        verify(noteService, never()).getAllNotes();
    }

    @Test
    void getNotesReturnsActiveNotesForUser() throws Exception {
        User user = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, List.of());
        Note note = new Note(10L, "Titulo", "Contenido", null, null, true, user);

        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(noteService.getActiveNotesByUser(1L, true)).thenReturn(List.of(note));

        mockMvc.perform(get("/api/notes/users/1/active")
                        .param("active", "true")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(noteService).getActiveNotesByUser(1L, true);
    }

    @Test
    void getNotesReturnsAllUserNotesWhenActiveParamIsMissing() throws Exception {
        User user = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, List.of());
        Note note = new Note(10L, "Titulo", "Contenido", null, null, true, user);

        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(noteService.getNotesByUser(1L)).thenReturn(List.of(note));

        mockMvc.perform(get("/api/notes/users/1/active")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));

        verify(noteService).getNotesByUser(1L);
    }

    @Test
    void getNotesReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notes/users/99/active")
                        .header("X-User-Id", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));

        verify(noteService, never()).getNotesByUser(any());
        verify(noteService, never()).getActiveNotesByUser(any(), any(Boolean.class));
    }

    @Test
    void getNoteByIdReturnsNoteForUser() throws Exception {
        Note note = new Note(10L, "Titulo", "Contenido", null, null, true, null);

        when(noteService.getNoteById(10L)).thenReturn(Optional.of(note));

        mockMvc.perform(get("/api/notes/users/1/notes/10")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Titulo"));

        verify(noteService).getNoteById(10L);
    }

    @Test
    void getNoteByIdReturnsNotFoundWhenNoteDoesNotExist() throws Exception {
        when(noteService.getNoteById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notes/users/1/notes/99")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Nota no encontrada"));
    }

    @Test
    void getNoteByIdReturnsForbiddenWhenRequesterCannotAccessUserPath() throws Exception {
        Note note = new Note(10L, "Titulo", "Contenido", null, null, true, null);

        when(noteService.getNoteById(10L)).thenReturn(Optional.of(note));
        when(userService.isAdmin(2L)).thenReturn(false);

        mockMvc.perform(get("/api/notes/users/1/notes/10")
                        .header("X-User-Id", 2L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    @Test
    void createNoteReturnsCreatedNote() throws Exception {
        User user = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, List.of());
        Note createdNote = new Note(10L, "Titulo", "Contenido", null, null, true, user);

        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(noteService.createNote(any(NoteDTONew.class), same(user))).thenReturn(createdNote);

        mockMvc.perform(post("/api/notes/users/1/notes")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titulo",
                                  "content": "Contenido"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Titulo"))
                .andExpect(jsonPath("$.content").value("Contenido"));

        ArgumentCaptor<NoteDTONew> noteCaptor = ArgumentCaptor.forClass(NoteDTONew.class);
        verify(noteService).createNote(noteCaptor.capture(), same(user));
        assertThat(noteCaptor.getValue().getTitle()).isEqualTo("Titulo");
        assertThat(noteCaptor.getValue().getContent()).isEqualTo("Contenido");
    }

    @Test
    void createNoteReturnsBadRequestWhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/notes/users/1/notes")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(noteService, never()).createNote(any(), any());
    }

    @Test
    void createNoteReturnsForbiddenWhenRequesterCannotAccessUserPath() throws Exception {
        User user = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, List.of());

        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(userService.isAdmin(2L)).thenReturn(false);

        mockMvc.perform(post("/api/notes/users/1/notes")
                        .header("X-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titulo",
                                  "content": "Contenido"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));

        verify(noteService, never()).createNote(any(), any());
    }

    @Test
    void editNoteReturnsOkWhenNoteExistsAndRequesterCanAccess() throws Exception {
        Note note = new Note(10L, "Viejo", "Contenido viejo", null, null, true, null);

        when(noteService.getNoteById(10L)).thenReturn(Optional.of(note));

        mockMvc.perform(put("/api/notes/users/1/notes/10")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Nuevo",
                                  "content": "Contenido nuevo"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Nota actualizada"))
                .andExpect(jsonPath("$.data.title").value("Nuevo"))
                .andExpect(jsonPath("$.data.content").value("Contenido nuevo"));

        ArgumentCaptor<NoteDTOEdit> noteCaptor = ArgumentCaptor.forClass(NoteDTOEdit.class);
        verify(noteService).editNote(eq(10L), noteCaptor.capture());
        assertThat(noteCaptor.getValue().getTitle()).isEqualTo("Nuevo");
        assertThat(noteCaptor.getValue().getContent()).isEqualTo("Contenido nuevo");
    }

    @Test
    void editNoteReturnsBadRequestWhenServiceRejectsEdit() throws Exception {
        Note note = new Note(10L, "Viejo", "Contenido viejo", null, null, true, null);

        when(noteService.getNoteById(10L)).thenReturn(Optional.of(note));
        org.mockito.Mockito.doThrow(new BadRequestException("Nota no encontrada"))
                .when(noteService).editNote(any(), any());

        mockMvc.perform(put("/api/notes/users/1/notes/10")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Nuevo",
                                  "content": "Contenido nuevo"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleActiveReturnsOkWhenNoteExistsAndRequesterCanAccess() throws Exception {
        Note note = new Note(10L, "Titulo", "Contenido", null, null, true, null);

        when(noteService.getNoteById(10L)).thenReturn(Optional.of(note));

        mockMvc.perform(patch("/api/notes/users/1/notes/10/toggle-active")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("estado de la nota actualizado"));

        verify(noteService).editStatusNote(10L);
    }

    @Test
    void deleteNoteReturnsOkWhenNoteExistsAndRequesterCanAccess() throws Exception {
        Note note = new Note(10L, "Titulo", "Contenido", null, null, true, null);

        when(noteService.getNoteById(10L)).thenReturn(Optional.of(note));

        mockMvc.perform(delete("/api/notes/users/1/notes/10")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Nota eliminada"));

        verify(noteService).deleteNote(10L);
    }

    @Test
    void requestWithoutUserHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/notes/users/1/active"))
                .andExpect(status().isBadRequest());
    }
}
