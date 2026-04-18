package com.hirelens.noteapp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hirelens.noteapp.models.Note;
import com.hirelens.noteapp.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNickname(String nickname);
    Optional<User> findByEmail(String email);
    List<Note> getNotesByUser(User user);
    
}
