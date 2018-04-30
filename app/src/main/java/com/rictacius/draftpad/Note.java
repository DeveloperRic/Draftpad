package com.rictacius.draftpad;

import android.content.Context;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;
import xyz.victorolaitan.easyjson.JSONElement;

/**
 * Created by Victor Olaitan on 19/02/2017.
 */

class Note {
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);

    private Context context;
    public UUID id;
    public String title = "";
    String body = "";
    Date created;
    Date edited;
    UUID[] childIDs;
    ArrayList<Note> children = new ArrayList<>();
    boolean isNoteGroup;
    boolean noteChildrenExpanded;
    int originalIndex;

    Note(Context context) {
        this.context = context;
    }

    boolean parse(EasyJSON noteData, ArrayList<Note> notes) {
        try {
            id = UUID.fromString((String) noteData.valueOf("id"));
            created = Note.dateFormat.parse((String) noteData.valueOf("created"));
            edited = Note.dateFormat.parse((String) noteData.valueOf("edited"));
            title = (String) noteData.valueOf("title");
            body = ((String) noteData.valueOf("body"));
            if (!noteData.elementExists("children")) {
                noteData.putArray("children");
                if (!save()) {
                    return false;
                }
            }
            JSONElement childrenData = noteData.search("children");
            childIDs = new UUID[childrenData.getChildren().size()];
            for (int i = 0; i < childIDs.length; i++) {
                childIDs[i] = UUID.fromString((String) ((JSONElement) childrenData.getChildren().get(i)).getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        originalIndex = notes.size();
        return true;
    }

    boolean save() {
        EasyJSON json = EasyJSON.create(getFile());

        json.putPrimitive("id", id.toString());
        json.putPrimitive("created", dateFormat.format(created));
        json.putPrimitive("edited", dateFormat.format(edited));
        json.putPrimitive("title", title);
        json.putPrimitive("body", body);
        json.putArray("children");
        for (Note child : children) {
            json.search("children").putPrimitive(child.id.toString());
        }

        try {
            json.save();
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public File getFile() {
        return new File(context.getFilesDir(), "NOTE_" + id.toString() + ".txt");
    }

    static class NoteDateInfo extends Note {
        int infoResID;
        Date date;
        private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMM YYYY", Locale.US);

        NoteDateInfo(Date date) {
            super(null);
            this.date = date;
        }

        NoteDateInfo(int infoResID) {
            super(null);
            this.infoResID = infoResID;
        }

        boolean save() {
            return true;
        }

        public File getFile() {
            return null;
        }

        String formatDate() {
            return monthFormat.format(date);
        }
    }
}
