package com.rictacius.draftpad;

import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        note = checkUnique(note);
        note.title = title;
        return note;
    }

    public static Note checkUnique(Note note) {
        if (notes.size() > 0) {
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
        } else {
            note.id = UUID.randomUUID();
        }
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

    public static void addNote(Note note) {
        notes.add(note);
        notifyChange();
    }

    private static void notifyChange() {
        boolean sorted = false;
        int pass = 0;
        while (!sorted) {
            int swaps = 0;
            for (int i = 0; i + 1 < notes.size() - pass; i++) {
                Note a = notes.get(i);
                Note b = notes.get(i + 1);
                if (a.edited.after(b.edited)) {
                    notes.set(i, b);
                    notes.set(i + 1, a);
                    swaps++;
                }
            }
            if (swaps == 0) {
                sorted = true;
            }
        }
        if (Dashboard.instance.notesRecyclerAdapter != null) {
            Dashboard.instance.notesRecyclerAdapter.notifyDataSetChanged();
        }
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
        notifyChange();
    }

    public static void createTestData() {
        notes.add(createNewNote("Note 1"));
        notes.add(createNewNote("Note 2"));
        notes.add(createNewNote("Note 3"));
        notes.add(createNewNote("Note 4"));
        notes.add(createNewNote("Note 5"));
        notifyChange();
    }

    public static void deleteNote(Note note) {
        note.getFile().delete();
        notes.remove(note);
        notifyChange();
    }
}
