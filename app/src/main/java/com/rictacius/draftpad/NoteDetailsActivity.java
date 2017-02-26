package com.rictacius.draftpad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.UUID;

public class NoteDetailsActivity extends AppCompatActivity {
    Note note;
    EditText txtTitle;
    EditText txtBody;
    TextView lblDone;
    Button btnDeleteNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        txtTitle = (EditText) findViewById(R.id.ND_txtTitle);
        txtBody = (EditText) findViewById(R.id.ND_txtBody);
        lblDone = (TextView) findViewById(R.id.ND_lblDone);
        btnDeleteNote = (Button) findViewById(R.id.ND_btnDeleteNote);

        Intent intent = getIntent();
        String ID_String = intent.getStringExtra("NOTE_ID");
        if (!ID_String.equals("")) {
            note = NoteManager.findNote(UUID.fromString(ID_String));
        } else {
            note = NoteManager.createNewNote("");
        }

        txtTitle.setText(note.title);
        txtBody.setText(note.body);
        lblDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                note.title = txtTitle.getText().toString();
                note.body = txtBody.getText().toString();
                note.saveNote();
                NoteManager.loadNotes();
                finish();
            }
        });
        btnDeleteNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(NoteDetailsActivity.this)
                        .setTitle("Draftpad")
                        .setMessage("Do you really want to delete this note?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                NoteManager.deleteNote(note);
                                finish();
                                Snackbar snackbar = Snackbar
                                        .make(Dashboard.instance.findViewById(R.id.Dash_CoordinatorLayout), "Note Deleted!", Snackbar.LENGTH_LONG)
                                        .setActionTextColor(Color.RED)
                                        .setAction("UNDO", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                note = NoteManager.checkUnique(note);
                                                note.saveNote();
                                                NoteManager.addNote(note);
                                            }
                                        });
                                snackbar.show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

    }
}
