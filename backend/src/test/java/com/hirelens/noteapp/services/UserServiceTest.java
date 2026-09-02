package com.hirelens.noteapp.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.hirelens.noteapp.dto.UserDTO;
import com.hirelens.noteapp.enums.Role;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.repositories.NoteRepository;
import com.hirelens.noteapp.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserRegistersUserWithEncodedPasswordAndUserRole() {
        UserDTO userDTO = new UserDTO("natalia", "natalia@mail.com", "secret", "secret");
        User savedUser = new User(1L, "natalia", "natalia@mail.com", "encoded-secret", Role.USER, java.util.List.of());

        when(userRepository.findByNickname("natalia")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("natalia@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(userDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User userToSave = userCaptor.getValue();

        assertThat(userToSave.getNickname()).isEqualTo("natalia");
        assertThat(userToSave.getEmail()).isEqualTo("natalia@mail.com");
        assertThat(userToSave.getPassword()).isEqualTo("encoded-secret");
        assertThat(userToSave.getRole()).isEqualTo(Role.USER);
        assertThat(userToSave.getNotes()).isEmpty();
        assertThat(result).isSameAs(savedUser);
    }

    @Test
    void createUserRejectsDuplicatedNickname() {
        UserDTO userDTO = new UserDTO("natalia", "new@mail.com", "secret", "secret");
        User existingUser = new User(1L, "natalia", "old@mail.com", "encoded", Role.USER, java.util.List.of());

        when(userRepository.findByNickname("natalia")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(userDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nickname ya en uso");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserRejectsDuplicatedEmail() {
        UserDTO userDTO = new UserDTO("newnick", "natalia@mail.com", "secret", "secret");
        User existingUser = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, java.util.List.of());

        when(userRepository.findByNickname("newnick")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("natalia@mail.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUser(userDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email ya en uso");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserRejectsPasswordMismatch() {
        UserDTO userDTO = new UserDTO("natalia", "natalia@mail.com", "secret", "other");

        when(userRepository.findByNickname("natalia")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("natalia@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(userDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Las contraseñas no coinciden");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticateReturnsUserForValidCredentials() {
        User existingUser = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, java.util.List.of());

        when(userRepository.findByEmail("natalia@mail.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        Optional<User> authenticated = userService.authenticate("natalia@mail.com", "secret");

        assertThat(authenticated).containsSame(existingUser);
    }

    @Test
    void authenticateReturnsEmptyForWrongPassword() {
        User existingUser = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, java.util.List.of());

        when(userRepository.findByEmail("natalia@mail.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        Optional<User> authenticated = userService.authenticate("natalia@mail.com", "wrong");

        assertThat(authenticated).isEmpty();
    }

    @Test
    void authenticateReturnsEmptyWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        Optional<User> authenticated = userService.authenticate("missing@mail.com", "secret");

        assertThat(authenticated).isEmpty();
        verify(passwordEncoder, never()).matches(any(), any());
    }
}
