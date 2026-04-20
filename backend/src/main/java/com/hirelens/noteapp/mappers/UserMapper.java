package com.hirelens.noteapp.mappers;

import org.springframework.stereotype.Component;

import com.hirelens.noteapp.dto.UserDTO;
import com.hirelens.noteapp.models.User;

@Component
public class UserMapper {
    

    public UserDTO toDTO(User user){
        if(user == null){
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setNickname(user.getNickname());
        userDTO.setEmail((user.getEmail()));
        userDTO.setRole(user.getRole());

        return userDTO;
    }
    
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User();
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());

        return user;
    }

}