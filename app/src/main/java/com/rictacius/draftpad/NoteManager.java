package com.rictacius.draftpad;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Victor Olaitan on 19/02/2017.
 */

public class NoteManager {
    public static ArrayList<Note> notes = new ArrayList<>();

    public static Note createNewNote(String title) {
        Note note = new Note();
        note = checkUnique(note);
        note.title = title;
        return note;
    }

    private static Note checkUnique(Note note) {
        UUID id = UUID.randomUUID();
        Boolean unique = false;
        while (!unique) {
            for (Note prevNote : notes) {
                if (prevNote.id.equals(note.id)) {
                    unique = false;
                } else {
                    unique = true;
                }
            }
        }
        return note;
    }

    public static List<Note> createTestData() {
        List<Note> notes = new ArrayList<>();
        notes.add(createNewNote("Note 1"));
        notes.add(createNewNote("Note 2"));
        notes.add(createNewNote("Note 3"));
        notes.add(createNewNote("Note 4"));
        notes.add(createNewNote("Note 5"));
        return notes;
    }
}
