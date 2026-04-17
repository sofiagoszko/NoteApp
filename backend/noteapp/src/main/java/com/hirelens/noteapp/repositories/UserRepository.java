package com.hirelens.noteapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hirelens.noteapp.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
}
