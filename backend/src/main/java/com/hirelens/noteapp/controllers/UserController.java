package com.hirelens.noteapp.controllers;
 
import com.hirelens.noteapp.dto.UserDTO;
import com.hirelens.noteapp.dto.UserDTOEdit;
import com.hirelens.noteapp.dto.UserDTOPass;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.services.UserService;
import com.hirelens.noteapp.responses.Response;
 
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
 
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "https://note-app-alpha-eight.vercel.app/"})
public class UserController {
 
    @Autowired
    private UserService userService;
 
    // POST /api/users/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            User user = userService.createUser(userDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("nickname", user.getNickname());
            response.put("email", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, e.getMessage(), e.getMessage()));
        }
    }
 
    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
 
        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, "Email y password requeridos", null));
        }
 
        boolean authenticated = userService.authenticateUser(email, password);
        if (!authenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response<>(false, "Credenciales inválidas", null) );
        }
 

        Optional<User> userOpt = userService.getUserByEmail(email);
        User user = userOpt.get();
 
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("nickname", user.getNickname());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }
 
    // GET /api/users/{id}
    @GetMapping("/{id}")
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
    public ResponseEntity<?> editUser(@PathVariable Long id, @Valid @RequestBody UserDTOEdit userDTO) {
        try {
            userService.editUser(id, userDTO);
            return ResponseEntity.ok(new Response<>(true, "Usuario actualizado", userDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
 
    // PATCH /api/users/{id}/password
    @PatchMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody UserDTOPass userDTO) {
        try {
            userService.changePassword(id, userDTO);
            return ResponseEntity.ok(new Response<>(true, "Contraseña actualizada", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
 
    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<?> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Usuario no encontrado", null));
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}