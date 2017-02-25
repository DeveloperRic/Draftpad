package com.rictacius.draftpad;

import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Victor Olaitan on 19/02/2017.
 */

public class NoteManager {
    public static ArrayList<Note> notes = new ArrayList<>();

    public static Note createNewNote(String title) {
        Note note = new Note();
        Date now = new Date();
        note.created = now;
        note.edited = now;
        if (notes.size() > 0) {
            note = checkUnique(note);
        } else {
            note.id = UUID.randomUUID();
        }
        note.title = title;
        return note;
    }

    private static Note checkUnique(Note note) {
        UUID id = null;
        Boolean unique = false;
        int tries = 0;
        while (!unique && tries <= 1000) {
            id = UUID.randomUUID();
            tries++;
            unique = true;
            for (Note prevNote : notes) {
                if (prevNote.id.equals(id)) {
                    unique = false;
                } else {
                    unique = true;
                }
            }
        }
        note.id = id;
        return note;
    }

    @Nullable
    public static Note findNote(UUID id) {
        for (Note note : notes) {
            if (note.id.equals(id)) {
                return note;
            }
        }
        return null;
    }

    public static void loadNotes() {
        notes.clear();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        for (File file : Dashboard.instance.getFilesDir().listFiles()) {
            UUID id = null;
            try {
                id = UUID.fromString(file.getName().replaceAll(".txt", ""));
            } catch (Exception e) {
                continue;
            }
            Note note = new Note();
            note.id = id;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("<DraftPad<NOTE_CREATED>>")) {
                        note.created = format.parse(line.replaceFirst("<DraftPad<NOTE_CREATED>>", ""));
                    } else if (line.startsWith("<DraftPad<NOTE_EDITED>>")) {
                        note.edited = format.parse(line.replaceFirst("<DraftPad<NOTE_EDITED>>", ""));
                    } else if (line.startsWith("<DraftPad<NOTE_TITLE>>")) {
                        note.title = line.replaceFirst("<DraftPad<NOTE_TITLE>>", "");
                    } else if (line.startsWith("<DraftPad<NOTE_BODY>>")) {
                        note.body = line.replaceFirst("<DraftPad<NOTE_BODY>>", "").replaceAll("<DraftPad<NOTE_NEW_LINE>>", "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            notes.add(note);
        }
    }

    public static void createTestData() {
        notes.add(createNewNote("Note 1"));
        notes.add(createNewNote("Note 2"));
        notes.add(createNewNote("Note 3"));
        notes.add(createNewNote("Note 4"));
        notes.add(createNewNote("Note 5"));
    }
}
