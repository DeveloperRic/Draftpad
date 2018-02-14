package com.rictacius.draftpad;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Victor Olaitan on 19/02/2017.
 */

class Note {
    private Context context;
    public UUID id;
    public String title = "";
    String body = "";
    Date created;
    Date edited;

    Note(Context context) {
        this.context = context;
    }

    void saveNote() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(id.toString() + ".txt", Context.MODE_PRIVATE)));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            writer.write("<DraftPad<NOTE_CREATED>>" + format.format(created));
            writer.newLine();
            writer.write("<DraftPad<NOTE_EDITED>>" + format.format(edited));
            writer.newLine();
            writer.write("<DraftPad<NOTE_TITLE>>" + title);
            writer.newLine();
            writer.write("<DraftPad<NOTE_BODY>>" + body.replaceAll("\n", "<DraftPad<NOTE_NEW_LINE>>"));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return new File(context.getFilesDir(), id + ".txt");
    }
}
