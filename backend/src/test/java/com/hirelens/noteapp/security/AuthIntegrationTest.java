package com.hirelens.noteapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.hirelens.noteapp.dto.NoteDTONew;
import com.hirelens.noteapp.dto.UserDTO;
import com.hirelens.noteapp.enums.Role;
import com.hirelens.noteapp.models.Note;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.repositories.NoteRepository;
import com.hirelens.noteapp.repositories.UserRepository;
import com.hirelens.noteapp.services.NoteService;
import com.hirelens.noteapp.services.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired NoteRepository noteRepository;
    @Autowired UserService userService;
    @Autowired NoteService noteService;
    @Autowired JwtService jwtService;

    @BeforeEach
    void clean() {
        noteRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser(String nick, String email) {
        return userService.createUser(new UserDTO(nick, email, "secret12", "secret12"));
    }

    private User createAdmin(String nick, String email) {
        User admin = createUser(nick, email);
        admin.setRole(Role.ADMIN);
        return userRepository.save(admin);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    @Test
    void registerReturnsTokenAndUser() throws Exception {
        mockMvc.perform(post("/api/users/register").header("X-Real-IP", "10.0.0.1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"neo\",\"email\":\"neo@x.com\",\"password\":\"secret12\",\"passwordConfirm\":\"secret12\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("neo@x.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void registerIgnoresClientSuppliedRole() throws Exception {
        mockMvc.perform(post("/api/users/register").header("X-Real-IP", "10.0.0.2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"trinity\",\"email\":\"trinity@x.com\",\"password\":\"secret12\",\"passwordConfirm\":\"secret12\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated());

        assertThat(userRepository.findByEmail("trinity@x.com").orElseThrow().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void loginReturnsTokenAndRejectsWrongPassword() throws Exception {
        createUser("morpheus", "morpheus@x.com");

        mockMvc.perform(post("/api/users/login").header("X-Real-IP", "10.0.0.3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"morpheus@x.com\",\"password\":\"secret12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/api/users/login").header("X-Real-IP", "10.0.0.4")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"morpheus@x.com\",\"password\":\"WRONG\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void notesRequireAuthentication() throws Exception {
        User user = createUser("cypher", "cypher@x.com");

        mockMvc.perform(get("/api/notes/users/{id}/active", user.getId()).header("X-Real-IP", "10.0.0.5"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/notes/users/{id}/active", user.getId())
                .header("X-Real-IP", "10.0.0.5")
                .header("Authorization", bearer(user)))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotReadAnotherUsersNotes() throws Exception {
        User a = createUser("userA", "a@x.com");
        User b = createUser("userB", "b@x.com");

        mockMvc.perform(get("/api/notes/users/{id}/active", b.getId())
                .header("X-Real-IP", "10.0.0.6")
                .header("Authorization", bearer(a)))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminCanListAllNotes() throws Exception {
        User user = createUser("plainUser", "plain@x.com");
        User admin = createAdmin("bossAdmin", "boss@x.com");

        mockMvc.perform(get("/api/notes/users")
                .header("X-Real-IP", "10.0.0.7")
                .header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/notes/users")
                .header("X-Real-IP", "10.0.0.7")
                .header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void cannotDeleteAnotherUsersNoteViaOwnPath() throws Exception {
        User a = createUser("owner", "owner@x.com");
        User b = createUser("attacker", "attacker@x.com");
        Note noteOfA = noteService.createNote(new NoteDTONew("secreto", "contenido"), a);

        mockMvc.perform(delete("/api/notes/users/{userId}/notes/{noteId}", b.getId(), noteOfA.getId())
                .header("X-Real-IP", "10.0.0.9")
                .header("Authorization", bearer(b)))
                .andExpect(status().isNotFound());

        assertThat(noteRepository.findById(noteOfA.getId())).isPresent();
    }
}
