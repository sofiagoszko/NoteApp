package com.hirelens.noteapp.services;

import org.springframework.stereotype.Service;

import com.hirelens.noteapp.models.Note;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.dto.UserDTO;
import com.hirelens.noteapp.repositories.NoteRepository;
import com.hirelens.noteapp.repositories.UserRepository;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<String> validateUser(UserDTO userDTO) {
        List<String> errors = new ArrayList<>();    

        if(getUserByNickname(userDTO.getNickname()).isPresent()) {
            errors.add("Nickname ya en uso");
        }

        if(getUserByEmail(userDTO.getEmail()).isPresent()) {
            errors.add("Email ya en uso");
        }

        return errors;
    }

    public void validatePassword(UserDTO userDTO) {
        if(!userDTO.getPassword().equals(userDTO.getPasswordConfirm())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }
    } 

    public User createUser(UserDTO userDTO) {
        List<String> validationErrors = validateUser(userDTO);
        validatePassword(userDTO);

        if(!validationErrors.isEmpty()) {
            throw new RuntimeException("Errores de validación: " + String.join(", ", validationErrors));
        }

        User user = new User();
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        return userRepository.save(user);
    }

    public void editUser(Long id, UserDTO userDTO) throws BadRequestException {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!existingUser.getNickname().equals(userDTO.getNickname()) &&
            getUserByNickname(userDTO.getNickname()).isPresent()) {
            throw new BadRequestException("Nickname ya en uso");
        }
        
        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
            getUserByEmail(userDTO.getEmail()).isPresent()) {
            throw new BadRequestException("Email ya en uso");
        }      

        existingUser.setNickname(userDTO.getNickname());
        existingUser.setEmail(userDTO.getEmail());      
        userRepository.save(existingUser);
    }

    public void changePassword(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validatePassword(userDTO);                
        existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));        
        userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean authenticateUser(String email, String password) {
        Optional<User> userOpt = getUserByEmail(email);
        return userOpt
            .map(user -> passwordEncoder.matches(password, user.getPassword()))
            .orElse(false);
    }

    public List<Note> getNotesByUser(Long userId) {
        return noteRepository.findByUserId(userId);   
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}
