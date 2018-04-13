package com.rictacius.draftpad;

import android.content.Context;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;

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
    int originalIndex;

    Note(Context context) {
        this.context = context;
    }

    boolean save() {
        EasyJSON json = EasyJSON.create(getFile());

        json.putPrimitive("id", id.toString());
        json.putPrimitive("created", dateFormat.format(created));
        json.putPrimitive("edited", dateFormat.format(edited));
        json.putPrimitive("title", title);
        json.putPrimitive("body", body);

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
