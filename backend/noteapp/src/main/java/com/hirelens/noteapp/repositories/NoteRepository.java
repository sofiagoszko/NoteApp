package com.hirelens.noteapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hirelens.noteapp.models.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
} 
