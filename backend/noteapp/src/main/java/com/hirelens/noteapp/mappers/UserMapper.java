package com.hirelens.noteapp.mappers;

import com.hirelens.noteapp.dto.UserDTO;
import com.hirelens.noteapp.models.User;

public class UserMapper {
    
    public UserMapper() {
    }

    public UserDTO toDTO(User user){
        if(user == null){
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setNickname(user.getNickname());
        userDTO.setEmail((user.getEmail()));
        userDTO.setPassword(user.getPassword());

        if(user.getNotes() != null){
            NoteMapper noteMapper = new NoteMapper();
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
        user.setPassword(userDTO.getPassword());

        if(userDTO.getNotes() != null){
            NoteMapper noteMapper = new NoteMapper();
            user.setNotes(userDTO.getNotes().stream().map(noteMapper::toEntity).toList());
        }

        return user;
    }


}
