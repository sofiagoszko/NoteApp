package com.hirelens.noteapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.hirelens.noteapp.enums.Role;
import com.hirelens.noteapp.models.Note;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.repositories.NoteRepository;
import com.hirelens.noteapp.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class BackendIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void cleanDatabase() {
        noteRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerUserPersistsUserAndReturnsCreatedResponse() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "natalia",
                                  "email": "natalia@mail.com",
                                  "password": "secret",
                                  "passwordConfirm": "secret"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("natalia"))
                .andExpect(jsonPath("$.email").value("natalia@mail.com"));

        User persistedUser = userRepository.findByEmail("natalia@mail.com").orElseThrow();
        assertThat(persistedUser.getNickname()).isEqualTo("natalia");
        assertThat(persistedUser.getRole()).isEqualTo(Role.USER);
        assertThat(persistedUser.getPassword()).isNotEqualTo("secret");
    }

    @Test
    void loginUsesPersistedUserForSuccessAndInvalidCredentials() throws Exception {
        registerUser("natalia", "natalia@mail.com", "secret");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "natalia@mail.com",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("natalia"))
                .andExpect(jsonPath("$.email").value("natalia@mail.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "natalia@mail.com",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void noteFlowCreatesListsEditsAndTogglesPersistedNote() throws Exception {
        User user = registerUser("natalia", "natalia@mail.com", "secret");

        mockMvc.perform(post("/api/notes/users/{userId}/notes", user.getId())
                        .header("X-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titulo inicial",
                                  "content": "Contenido inicial"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Titulo inicial"))
                .andExpect(jsonPath("$.content").value("Contenido inicial"))
                .andExpect(jsonPath("$.active").value(true));

        List<Note> persistedNotes = noteRepository.findByUserId(user.getId());
        assertThat(persistedNotes).hasSize(1);
        Note note = persistedNotes.get(0);
        assertThat(note.getCreatedAt()).isNotNull();

        mockMvc.perform(get("/api/notes/users/{userId}/active", user.getId())
                        .param("active", "true")
                        .header("X-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(note.getId()))
                .andExpect(jsonPath("$[0].title").value("Titulo inicial"));

        mockMvc.perform(put("/api/notes/users/{userId}/notes/{noteId}", user.getId(), note.getId())
                        .header("X-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titulo editado",
                                  "content": "Contenido editado"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Nota actualizada"));

        Note editedNote = noteRepository.findById(note.getId()).orElseThrow();
        assertThat(editedNote.getTitle()).isEqualTo("Titulo editado");
        assertThat(editedNote.getContent()).isEqualTo("Contenido editado");
        assertThat(editedNote.getUpdatedAt()).isNotNull();

        mockMvc.perform(patch("/api/notes/users/{userId}/notes/{noteId}/toggle-active", user.getId(), note.getId())
                        .header("X-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("estado de la nota actualizado"));

        assertThat(noteRepository.findById(note.getId()).orElseThrow().isActive()).isFalse();

        mockMvc.perform(get("/api/notes/users/{userId}/active", user.getId())
                        .param("active", "false")
                        .header("X-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(note.getId()))
                .andExpect(jsonPath("$[0].active").value(false));
    }

    @Test
    void nonOwnerCannotAccessNotesThroughAnotherUsersPath() throws Exception {
        User owner = registerUser("owner", "owner@mail.com", "secret");
        User requester = registerUser("requester", "requester@mail.com", "secret");
        Note note = createNote(owner, "Privada", "Contenido privado");

        mockMvc.perform(get("/api/notes/users/{userId}/notes/{noteId}", owner.getId(), note.getId())
                        .header("X-User-Id", requester.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    private User registerUser(String nickname, String email, String password) throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "%s",
                                  "email": "%s",
                                  "password": "%s",
                                  "passwordConfirm": "%s"
                                }
                                """.formatted(nickname, email, password, password)))
                .andExpect(status().isCreated());

        return userRepository.findByEmail(email).orElseThrow();
    }

    private Note createNote(User user, String title, String content) throws Exception {
        mockMvc.perform(post("/api/notes/users/{userId}/notes", user.getId())
                        .header("X-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "content": "%s"
                                }
                                """.formatted(title, content)))
                .andExpect(status().isCreated());

        return noteRepository.findByUserId(user.getId()).get(0);
    }
}
