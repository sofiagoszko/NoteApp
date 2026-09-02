package com.hirelens.noteapp.controllers;

import com.hirelens.noteapp.dto.AuthResponse;
import com.hirelens.noteapp.dto.LoginRequest;
import com.hirelens.noteapp.dto.UserDTO;
import com.hirelens.noteapp.dto.UserDTOEdit;
import com.hirelens.noteapp.dto.UserDTOPass;
import com.hirelens.noteapp.exceptions.InvalidCredentialsException;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.security.JwtService;
import com.hirelens.noteapp.services.UserService;
import com.hirelens.noteapp.responses.Response;

import jakarta.validation.Valid;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    // POST /api/users/register  
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.createUser(userDTO);
        String token = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.of(token, user));
    }

    // POST /api/users/login 
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest credentials) {
        User user = userService.authenticate(credentials.email(), credentials.password())
                .orElseThrow(InvalidCredentialsException::new);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(AuthResponse.of(token, user));
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    @PreAuthorize("@authz.isSelfOrAdmin(#id, authentication)")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Usuario no encontrado", null));
        }
        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("nickname", user.getNickname());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("notes", user.getNotes());
        return ResponseEntity.ok(response);
    }

    // PUT /api/users/{id}
    @PutMapping("/{id}")
    @PreAuthorize("@authz.isSelfOrAdmin(#id, authentication)")
    public ResponseEntity<?> editUser(@PathVariable Long id, @Valid @RequestBody UserDTOEdit userDTO)
            throws BadRequestException {
        userService.editUser(id, userDTO);
        return ResponseEntity.ok(new Response<>(true, "Usuario actualizado", userDTO));
    }

    // PATCH /api/users/{id}/password
    @PatchMapping("/{id}/password")
    @PreAuthorize("@authz.isSelfOrAdmin(#id, authentication)")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody UserDTOPass userDTO) {
        userService.changePassword(id, userDTO);
        return ResponseEntity.ok(new Response<>(true, "Contraseña actualizada", null));
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.isSelfOrAdmin(#id, authentication)")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<?> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Usuario no encontrado", null));
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
