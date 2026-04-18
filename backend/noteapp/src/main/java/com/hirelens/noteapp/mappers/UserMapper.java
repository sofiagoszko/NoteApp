package com.hirelens.noteapp.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hirelens.noteapp.dto.UserDTO;
import com.hirelens.noteapp.models.User;

@Component
public class UserMapper {
    
    @Autowired
    private NoteMapper noteMapper;

    public UserDTO toDTO(User user){
        if(user == null){
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setNickname(user.getNickname());
        userDTO.setEmail((user.getEmail()));
        userDTO.setRole(user.getRole());

        if(user.getNotes() != null){
            userDTO.setNotes(user.getNotes().stream().map(noteMapper::toDTO).toList());
        }

        return userDTO;
    }
    
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User();
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());

        if(userDTO.getNotes() != null){
            NoteMapper noteMapper = new NoteMapper();
            user.setNotes(userDTO.getNotes().stream().map(noteMapper::toEntity).toList());
        }

        return user;
    }

}