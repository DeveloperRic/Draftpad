package com.rictacius.draftpad;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;

/**
 * Created by Victor Olaitan on 19/02/2017.
 */

class NoteManager {
    ArrayList<Note> notes = new ArrayList<>();
    private String previousFilter = "";
    private ArrayList<Note> filteredNotes = new ArrayList<>();
    private NoteComparator noteComparator = new NoteComparator();
    boolean notesSorted, notesLoaded;

    private NoteManager() {
    }

    static Pair<NoteManager, ErrorCode> createNoteManagerFromLegacy() {
        NoteManager noteManager = new NoteManager();
        ErrorCode result = noteManager.loadLegacyNotes();
        if (result == ErrorCode.NO_ERROR) {
            if (!noteManager.saveAll()) {
                result = ErrorCode.SAVE_ERROR;
                noteManager.notesLoaded = false;
            } else {
                noteManager.notesLoaded = true;
            }
        } else {
            noteManager.notesLoaded = false;
        }
        return new Pair<>(noteManager, result);
    }

    static Pair<NoteManager, ErrorCode> createNoteManager(Context context) {
        NoteManager noteManager = new NoteManager();
        noteManager.notes.clear();
        noteManager.notesLoaded = true;
        for (File file : context.getFilesDir().listFiles()) {
            if (file.getName().startsWith("NOTE_")) {
                try {
                    String ID = String.valueOf(file.getName()
                            .replaceAll("NOTE_", "").replaceAll(".txt", ""));
                    EasyJSON noteData = EasyJSON.open(new File(context.getFilesDir(), "NOTE_" + ID + ".txt"));
                    if (!noteManager.loadNote(noteData)) {
                        noteManager.notesLoaded = false;
                    }
                } catch (IOException | EasyJSONException e) {
                    e.printStackTrace();
                    noteManager.notesLoaded = false;
                }
            }
        }
        return new Pair<>(noteManager, noteManager.notesLoaded ? ErrorCode.NO_ERROR : ErrorCode.NOTE_MALFORMED);
    }

    private boolean loadNote(EasyJSON noteData) {
        Note note = new Note(ActivityManager.getDashboardActivity());
        try {
            note.id = UUID.fromString((String) noteData.valueOf("id"));
            note.created = Note.dateFormat.parse((String) noteData.valueOf("created"));
            note.edited = Note.dateFormat.parse((String) noteData.valueOf("edited"));
            note.title = (String) noteData.valueOf("title");
            note.body = ((String) noteData.valueOf("body"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        note.originalIndex = notes.size();
        notes.add(note);
        return true;
    }

    private ErrorCode loadLegacyNotes() {
        try {
            int size = 0;
            for (File file : ActivityManager.getDashboardActivity().getFilesDir().listFiles()) {
                UUID id;
                try {
                    id = UUID.fromString(file.getName().replaceAll(".txt", ""));
                } catch (Exception e) {
                    continue;
                }
                size++;
                Note note = new Note(ActivityManager.getDashboardActivity());
                note.id = id;
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("<DraftPad<NOTE_CREATED>>")) {
                            note.created = Note.dateFormat.parse(line.replaceFirst("<DraftPad<NOTE_CREATED>>", ""));
                        } else if (line.startsWith("<DraftPad<NOTE_EDITED>>")) {
                            note.edited = Note.dateFormat.parse(line.replaceFirst("<DraftPad<NOTE_EDITED>>", ""));
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
                note.originalIndex = notes.size();
                notes.add(note);
            }
            if (notes.size() != size) {
                return ErrorCode.NOTE_MALFORMED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorCode.FILE_NO_ACCESS;
        }
        return ErrorCode.NO_ERROR;
    }

    boolean saveAll() {
        boolean allSaved = true;
        for (Note note : notes) {
            if (!note.save()) {
                allSaved = false;
            }
        }
        return allSaved;
    }

    Note createNewNote() {
        Note note = new Note(ActivityManager.getDashboardActivity());
        Date now = new Date();
        note.created = now;
        note.edited = now;
        checkUnique(note);
        return note;
    }

    private void checkUnique(Note note) {
        if (notes.size() > 0) {
            UUID id = null;
            Boolean unique = false;
            int tries = 0;
            while (!unique && tries <= 1000) {
                id = UUID.randomUUID();
                tries++;
                unique = true;
                for (Note prevNote : notes) {
                    if (!(prevNote instanceof Note.NoteDateInfo)) {
                        unique = !prevNote.id.equals(id);
                    }
                }
            }
            note.id = id;
        } else {
            note.id = UUID.randomUUID();
        }
    }

    @Nullable
    Note findNote(UUID id) {
        for (Note note : notes) {
            if (!(note instanceof Note.NoteDateInfo)) {
                if (note.id.equals(id)) {
                    return note;
                }
            }
        }
        return null;
    }

    void addNote(Note note) {
        if (notes.size() == 0) {
            toggleNoNotesLabel(false);
        }
        if (notesSorted) {
            if (!(notes.get(0) instanceof Note.NoteDateInfo)) {
                notes.add(0, new Note.NoteDateInfo(note.edited));
            }
            notes.add(1, note);
            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyItemInserted(1);
        } else {
            notes.add(note);
            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyItemInserted(notes.size() - 1);
        }
    }

    private int findLocation(Note note) {
        for (int i = 0; i < notes.size(); i++) {
            if (!(notes.get(i) instanceof Note.NoteDateInfo)) {
                if (notes.get(i).id.equals(note.id)) {
                    return i;
                }
            }
        }
        return -1;
    }

    void toggleNoNotesLabel(boolean visible) {
        ActivityManager.getDashboardActivity().findViewById(R.id.Dash_noNotesLabel).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /*
    public void createTestData() {
        notes.add(createNewNote("Note 1"));
        notes.add(createNewNote("Note 2"));
        notes.add(createNewNote("Note 3"));
        notes.add(createNewNote("Note 4"));
        notes.add(createNewNote("Note 5"));
    }
    */

    boolean deleteNote(Note note) {
        return !note.getFile().exists() || note.getFile().delete();
    }

    int hideNote(Note note) {
        int position = findLocation(note);
        if (position >= 0) {
            notes.remove(note);
            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyItemRemoved(position);
            return position;
        }
        return -1;
    }

    void unhideNote(Note note, int position) {
        if (position >= 0) {
            notes.add(position, note);
            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyItemInserted(position);
        }
    }

    void sortNotes() {
        if (notes.size() >= 2) {

            noteComparator.isSorting = true;
            Collections.sort(notes, noteComparator);
            groupNotes();

            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyDataSetChanged();
            notesSorted = true;
        }
    }

    void unsortNotes() {
        if (notes.size() >= 2) {

            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i) instanceof Note.NoteDateInfo) {
                    notes.remove(i);
                    ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyItemRemoved(i);
                    i--;
                }
            }
            noteComparator.isSorting = false;
            Collections.sort(notes, noteComparator);

            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyDataSetChanged();
            notesSorted = false;
        }
    }

    private void groupNotes() {
        if (notes.isEmpty()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int week = calendar.get(Calendar.WEEK_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);

        boolean setWeek = false, setThisMonth = false;
        Set<String> setMonths = new HashSet<>();

        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            calendar.setTime(note.edited);
            int noteMonth = calendar.get(Calendar.MONTH);
            if (noteMonth == month) {
                if (calendar.get(Calendar.WEEK_OF_MONTH) == week) {
                    if (!setWeek) {
                        notes.add(i, new Note.NoteDateInfo(R.string.dashList_thisWeekLabel));
                        ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyItemInserted(i);
                        setWeek = true;
                    }
                } else if (!setThisMonth) {
                    notes.add(i, new Note.NoteDateInfo(R.string.dashList_thisMonthLabel));
                    ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyItemInserted(i);
                    setThisMonth = true;
                }
            } else {
                String record = noteMonth + "/" + calendar.get(Calendar.YEAR);
                if (!setMonths.contains(record)) {
                    notes.add(i, new Note.NoteDateInfo(note.edited));
                    ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyItemInserted(i);
                    setMonths.add(record);
                }
            }
        }
    }

    void filterNotes(String query) {
        String checkString = previousFilter;
        if (query.length() < previousFilter.length() && query.length() != 0) {
            checkString = previousFilter.substring(0, query.length());
        }

        if (query.startsWith(checkString) || query.equals("")) {
            for (int i = 0; i < filteredNotes.size(); i++) {
                Note note = filteredNotes.get(i);
                if (matchesFilter(query, note)) {
                    notes.add(filteredNotes.remove(i));
                    i--;
                }
            }
            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyDataSetChanged();
        }

        if (!query.equals("")) {

            for (int i = 0; i < notes.size(); i++) {
                if (!matchesFilter(query, notes.get(i))) {
                    filteredNotes.add(notes.remove(i));
                    ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyItemRemoved(i);
                    i--;
                }
            }
        }

        previousFilter = query;
    }

    private boolean matchesFilter(String filter, Note note) {
        return note.title.contains(filter);
    }

    static class NoteComparator implements Comparator<Note> {

        boolean isSorting;

        @Override
        public int compare(Note a, Note b) {
            if (isSorting) {
                if (a.edited.after(b.edited)) {
                    return -1;
                } else if (a.edited.before(b.edited)) {
                    return 1;
                }
            } else {
                if (a.originalIndex > b.originalIndex) {
                    return 1;
                } else if (a.originalIndex < b.originalIndex) {
                    return -1;
                }
            }
            return 0;
        }

    }
}
