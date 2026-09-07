package com.hirelens.noteapp.dto;

import com.hirelens.noteapp.enums.Role;
import com.hirelens.noteapp.models.User;


public record AuthResponse(String token, UserSummary user) {

    public record UserSummary(Long id, String nickname, String email, Role role) {

        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getNickname(), user.getEmail(), user.getRole());
        }
    }

    public static AuthResponse of(String token, User user) {
        return new AuthResponse(token, UserSummary.from(user));
    }
}
