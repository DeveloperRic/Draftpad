package com.rictacius.draftpad;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;

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

class NoteManager extends Dashboard {
    ArrayList<Note> notes = new ArrayList<>();

    Note createNewNote(String title) {
        Note note = new Note(Dashboard.dash.context);
        Date now = new Date();
        note.created = now;
        note.edited = now;
        note = checkUnique(note);
        note.title = title;
        return note;
    }

    Note checkUnique(Note note) {
        if (notes.size() > 0) {
            UUID id = null;
            Boolean unique = false;
            int tries = 0;
            while (!unique && tries <= 1000) {
                id = UUID.randomUUID();
                tries++;
                unique = true;
                for (Note prevNote : notes) {
                    unique = !prevNote.id.equals(id);
                }
            }
            note.id = id;
        } else {
            note.id = UUID.randomUUID();
        }
        return note;
    }

    @Nullable
    Note findNote(UUID id) {
        for (Note note : notes) {
            if (note.id.equals(id)) {
                return note;
            }
        }
        return null;
    }

    void addNote(Note note) {
        notes.add(note);
        Dashboard.dash.notesRecyclerAdapter().notifyItemInserted(notes.size() - 1);
    }

    int findLocation(Note note) {
        int position = -1;
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).equals(note)) {
                position = i;
            }
        }
        return position;
    }

    void insertNote(Note note, int position) {
        notes.add(position, note);
        Dashboard.dash.notesRecyclerAdapter().notifyItemInserted(position);
    }

    /*
    private void notifyChange() {
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
        /*
        if (Dashboard.instance.notesRecyclerAdapter != null) {
            Dashboard.instance.notesRecyclerAdapter.notifyDataSetChanged();
        }

        if (notes.size() == 0) {
            Dashboard.findView(R.id.Dash_noNotesLabel).init(View.GONE);
        }
    }
    */

    void toggleNoNotesLabel(boolean visible) {
        if (dash.noteManager.notes.size() == 0) {
            View view = Dashboard.dash.activity().findViewById(R.id.Dash_noNotesLabel);
            if ((visible && view.getVisibility() != View.VISIBLE) || (!visible && view.getVisibility() != View.GONE)) {
                view.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    boolean loadNotes() {
        notes.clear();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try {
            int i = 0;
            for (File file : Dashboard.dash.context.getFilesDir().listFiles()) {
                UUID id;
                try {
                    id = UUID.fromString(file.getName().replaceAll(".txt", ""));
                } catch (Exception e) {
                    continue;
                }
                Note note = new Note(Dashboard.dash.context);
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
                i++;
            }
        } catch (Exception e) {
            Dashboard.showSnackBarWithAction("Failed to refresh notes!", Snackbar.LENGTH_LONG, Color.RED, "RETRY", new Runnable() {
                @Override
                public void run() {
                    loadNotes();
                }
            });
            return false;
        }
        return true;
    }

    /*
    public void createTestData() {
        notes.add(createNewNote("Note 1"));
        notes.add(createNewNote("Note 2"));
        notes.add(createNewNote("Note 3"));
        notes.add(createNewNote("Note 4"));
        notes.add(createNewNote("Note 5"));
        notifyChange();
    }
    */

    boolean deleteNote(Note note) {
        return deleteNote(note, -1);
    }

    boolean deleteNote(Note note, int pos) {
        if (pos == -1) {
            int position = findLocation(note);
            if (position >= 0) {
                if (note.getFile().delete()) {
                    notes.remove(note);
                    Dashboard.dash.notesRecyclerAdapter().notifyItemRemoved(position);
                    return true;
                }
            }
        } else {
            if (note.getFile().delete()) {
                notes.remove(note);
                Dashboard.dash.notesRecyclerAdapter().notifyItemRemoved(pos);
                return true;
            }
        }
        return false;
    }

    int hideNote(Note note) {
        int position = findLocation(note);
        if (position >= 0) {
            notes.remove(note);
            Dashboard.dash.notesRecyclerAdapter().notifyItemRemoved(position);
            return position;
        }
        return -1;
    }

    void unhideNote(Note note, int position) {
        if (position >= 0) {
            notes.add(position, note);
            Dashboard.dash.notesRecyclerAdapter().notifyItemInserted(position);
        }
    }
}
