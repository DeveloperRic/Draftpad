package com.rictacius.draftpad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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

        ActivityManager.initActivity(this);
        ActivityManager.AppBar.init(this, ActivityManager.AppBar.VisibilityToken.SHOW_EXTRA);
        ActivityManager.AppBar.setInputTextHint(this, R.string.noteDetails_titleHint);
        ActivityManager.AppBar.setButtonName(this, R.string.noteDetails_save);

        txtTitle = ActivityManager.AppBar.getInputText(this);
        txtBody = findViewById(R.id.ND_txtBody);
        lblDone = ActivityManager.AppBar.getButton(this);
        btnDeleteNote = findViewById(R.id.ND_btnDeleteNote);

        Intent intent = getIntent();
        final String ID_String = intent.getStringExtra("NOTE_ID");
        if (!ID_String.equals("")) {
            note = Dashboard.dash.noteManager.findNote(UUID.fromString(ID_String));
            ActivityManager.AppBar.setTitleText(this, R.string.noteDetails_appBarEdit);
        } else {
            note = Dashboard.dash.noteManager.createNewNote("");
            btnDeleteNote.setEnabled(false);
            btnDeleteNote.setBackgroundColor(Color.GRAY);
            ActivityManager.AppBar.setTitleText(this, R.string.noteDetails_appBarCreate);
        }

        if (note == null) {
            finish();
            overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
            Dashboard.showSnackBarWithAction("Could not initialise note!", Snackbar.LENGTH_LONG, Color.RED, "RETRY", new Runnable() {
                @Override
                public void run() {
                    Dashboard.openNote(Dashboard.dash.context, ID_String);
                }
            });
        }

        txtTitle.setText(note.title);
        txtBody.setText(note.body);
        lblDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Dashboard.dash.noteManager.notes.size() == 0) {
                    Dashboard.dash.noteManager.toggleNoNotesLabel(false);
                }
                note.title = txtTitle.getText().toString();
                note.body = txtBody.getText().toString();
                note.saveNote();
                sendNotesRefresh();
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

                                final int position = Dashboard.dash.noteManager.hideNote(note);

                                final Handler deleteHandler = new Handler();
                                final Runnable deleteRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (Dashboard.dash.noteManager.notes.size() == 1) {
                                            Dashboard.dash.noteManager.toggleNoNotesLabel(true);
                                        }
                                        Dashboard.dash.noteManager.deleteNote(note, position);
                                    }
                                };

                                deleteHandler.postDelayed(deleteRunnable, 3500);

                                finish();
                                overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);

                                Dashboard.showSnackBarWithAction("Note Deleted!", Snackbar.LENGTH_LONG, Color.RED, "UNDO", new Runnable() {
                                    @Override
                                    public void run() {
                                        deleteHandler.removeCallbacks(deleteRunnable);
                                        Dashboard.dash.noteManager.toggleNoNotesLabel(false);
                                        Dashboard.dash.noteManager.unhideNote(note, position);
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

    }

    void sendNotesRefresh() {
        if (Dashboard.dash.currentNoteView != null) {
            Dashboard.dash.currentNoteView.updateInfo();
        } else {
            setResult(Activity.RESULT_OK);
        }
        finish();
        overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
    }

    int previousNoteLocation;

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(NoteDetailsActivity.this)
                .setTitle("Draftpad")
                .setMessage("Do you want to save your progress before leaving?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        exitActivity();
                    }
                }).show();
    }

    private void exitActivity() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
    }

    /*
    <include
        layout="@layout/app_toolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
     */

    /**/
}
