package com.rictacius.draftpad;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static android.provider.Telephony.Mms.Part.FILENAME;

/**
 * Created by Victor Olaitan on 19/02/2017.
 */

public class Note {
    public UUID id;
    public String title = "";
    public String body = "";
    public Date created;
    public Date edited;

    public Note() {
    }

    public void saveNote() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Dashboard.instance.openFileOutput(id.toString() + ".txt", Context.MODE_PRIVATE)));
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
        return new File(Dashboard.instance.getFilesDir(), id + ".txt");
    }
}
