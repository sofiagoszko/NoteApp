package com.hirelens.noteapp.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hirelens.noteapp.dto.NoteDTOEdit;
import com.hirelens.noteapp.dto.NoteDTONew;
import com.hirelens.noteapp.enums.Role;
import com.hirelens.noteapp.models.Note;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.repositories.NoteRepository;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void createNoteCreatesActiveNoteForUser() {
        User user = new User(1L, "natalia", "natalia@mail.com", "encoded", Role.USER, List.of());
        NoteDTONew noteDTO = new NoteDTONew("Titulo", "Contenido");
        Note savedNote = new Note(10L, "Titulo", "Contenido", null, null, true, user);

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        Note result = noteService.createNote(noteDTO, user);

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());
        Note noteToSave = noteCaptor.getValue();

        assertThat(noteToSave.getTitle()).isEqualTo("Titulo");
        assertThat(noteToSave.getContent()).isEqualTo("Contenido");
        assertThat(noteToSave.getCreatedAt()).isNotNull();
        assertThat(noteToSave.isActive()).isTrue();
        assertThat(noteToSave.getUser()).isSameAs(user);
        assertThat(result).isSameAs(savedNote);
    }

    @Test
    void editNoteUpdatesExistingNote() throws BadRequestException {
        Note existingNote = new Note(10L, "Viejo", "Contenido viejo", null, null, true, null);
        NoteDTOEdit noteDTO = new NoteDTOEdit("Nuevo", "Contenido nuevo", null);

        when(noteRepository.findById(10L)).thenReturn(Optional.of(existingNote));

        noteService.editNote(10L, noteDTO);

        assertThat(existingNote.getTitle()).isEqualTo("Nuevo");
        assertThat(existingNote.getContent()).isEqualTo("Contenido nuevo");
        assertThat(existingNote.getUpdatedAt()).isNotNull();
        verify(noteRepository).save(existingNote);
    }

    @Test
    void editNoteThrowsWhenNoteDoesNotExist() {
        NoteDTOEdit noteDTO = new NoteDTOEdit("Nuevo", "Contenido nuevo", null);

        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.editNote(99L, noteDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Nota no encontrada");

        verify(noteRepository, never()).save(any());
    }

    @Test
    void editStatusNoteArchivesActiveNote() throws BadRequestException {
        Note existingNote = new Note(10L, "Titulo", "Contenido", null, null, true, null);

        when(noteRepository.findById(10L)).thenReturn(Optional.of(existingNote));

        noteService.editStatusNote(10L);

        assertThat(existingNote.isActive()).isFalse();
        verify(noteRepository).save(existingNote);
    }

    @Test
    void editStatusNoteRestoresArchivedNote() throws BadRequestException {
        Note existingNote = new Note(10L, "Titulo", "Contenido", null, null, false, null);

        when(noteRepository.findById(10L)).thenReturn(Optional.of(existingNote));

        noteService.editStatusNote(10L);

        assertThat(existingNote.isActive()).isTrue();
        verify(noteRepository).save(existingNote);
    }

    @Test
    void editStatusNoteThrowsWhenNoteDoesNotExist() {
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.editStatusNote(99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Nota no encontrada");

        verify(noteRepository, never()).save(any());
    }

    @Test
    void getNotesByUserReturnsRepositoryResult() {
        List<Note> notes = List.of(new Note(10L, "Titulo", "Contenido", null, null, true, null));

        when(noteRepository.findByUserId(1L)).thenReturn(notes);

        List<Note> result = noteService.getNotesByUser(1L);

        assertThat(result).isSameAs(notes);
    }

    @Test
    void getActiveNotesByUserReturnsRepositoryResult() {
        List<Note> notes = List.of(new Note(10L, "Titulo", "Contenido", null, null, true, null));

        when(noteRepository.findByUserIdAndActive(1L, true)).thenReturn(notes);

        List<Note> result = noteService.getActiveNotesByUser(1L, true);

        assertThat(result).isSameAs(notes);
    }

    @Test
    void getNoteByIdReturnsRepositoryResult() {
        Note note = new Note(10L, "Titulo", "Contenido", null, null, true, null);

        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));

        Optional<Note> result = noteService.getNoteById(10L);

        assertThat(result).containsSame(note);
    }

    @Test
    void deleteNoteDeletesById() {
        noteService.deleteNote(10L);

        verify(noteRepository).deleteById(10L);
    }
}
