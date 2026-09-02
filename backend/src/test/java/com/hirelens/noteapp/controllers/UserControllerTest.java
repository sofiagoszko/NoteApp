package com.hirelens.noteapp.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hirelens.noteapp.config.SecurityConfig;
import com.hirelens.noteapp.enums.Role;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.services.UserService;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void registerReturnsCreatedUser() throws Exception {
        User createdUser = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, List.of());

        when(userService.createUser(any())).thenReturn(createdUser);

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
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nickname").value("natalia"))
                .andExpect(jsonPath("$.email").value("natalia@mail.com"));
    }

    @Test
    void registerReturnsBadRequestWhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "",
                                  "email": "mail-invalido",
                                  "password": "",
                                  "passwordConfirm": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    void registerReturnsBadRequestWhenServiceRejectsDuplicatedUser() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new RuntimeException("Errores de validación: Email ya en uso"));

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Errores de validación: Email ya en uso"))
                .andExpect(jsonPath("$.data").value("Errores de validación: Email ya en uso"));
    }

    @Test
    void loginReturnsUserWhenCredentialsAreValid() throws Exception {
        User user = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, List.of());

        when(userService.authenticateUser("natalia@mail.com", "secret")).thenReturn(true);
        when(userService.getUserByEmail("natalia@mail.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "natalia@mail.com",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nickname").value("natalia"))
                .andExpect(jsonPath("$.email").value("natalia@mail.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(userService.authenticateUser("natalia@mail.com", "wrong")).thenReturn(false);

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
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void loginReturnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "natalia@mail.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email y password requeridos"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(userService, never()).authenticateUser(any(), any());
    }
}
