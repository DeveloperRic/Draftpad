package com.rictacius.draftpad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;

public class NoteDetailsActivity extends AppCompatActivity {
    Note note;
    FormatBar formatBar;
    EditText txtTitle;
    EditText txtBody;
    boolean hasUnsavedChanges, isNewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        Intent intent = getIntent();
        final String ID_String = intent.getStringExtra("NOTE_ID");
        if (!ID_String.equals("")) {
            note = ActivityManager.getNoteManager().findNote(UUID.fromString(ID_String));
            setTitle(R.string.noteDetails_appBarEdit);
        } else {
            isNewNote = true;
            note = ActivityManager.getNoteManager().createNewNote();
            setTitle(R.string.noteDetails_appBarCreate);
        }

        if (note == null) {
            finish();
            overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
            ActivityManager.getDashboardActivity().notifyFailedToInit(ID_String);
        }

        setSupportActionBar((Toolbar) findViewById(R.id.ND_toolbar));

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        txtTitle = findViewById(R.id.ND_txtTitle);
        txtBody = findViewById(R.id.ND_txtBody);

        txtTitle.setText(note.title);
        txtTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                hasUnsavedChanges = !editable.toString().equals(note.title);
            }
        });

        txtBody.setText(note.body);
        txtBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                hasUnsavedChanges = !editable.toString().equals(note.body);
            }
        });
        txtBody.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    formatBar.show();
                } else {
                    formatBar.hide();
                }
            }
        });

        formatBar = new FormatBar(this, note, txtBody);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_details, menu);

        menu.findItem(R.id.ND_appbar_action_delete).setEnabled(!isNewNote);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                checkSaved();
                return true;
            case R.id.ND_appbar_action_save:
                saveNote(note, false);
                return true;
            case R.id.ND_appbar_action_delete:
                AlertDialog.Builder confirmDelete = new AlertDialog.Builder(NoteDetailsActivity.this)
                        .setTitle(R.string.app_name)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                final int position = ActivityManager.getNoteManager().hideNote(note);

                                final Handler deleteHandler = new Handler();
                                final Runnable deleteRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ActivityManager.getNoteManager().notes.size() == 1) {
                                            ActivityManager.getNoteManager().toggleNoNotesLabel(true);
                                        }
                                        if (!ActivityManager.getNoteManager().deleteNote(note)) {
                                            ActivityManager.getDashboardActivity().notifyNoteNotDeleted(note);
                                        }
                                    }
                                };

                                deleteHandler.postDelayed(deleteRunnable, 3500);

                                finish();
                                overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);

                                ActivityManager.getDashboardActivity().notifyNoteDeleted(deleteHandler, deleteRunnable, note, position);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null);
                if (note.children.size() == 0) {
                    confirmDelete.setMessage(R.string.noteDetails_confirmDeleteNormal);
                } else {
                    confirmDelete.setMessage(R.string.noteDetails_confirmDeleteGroup);
                }
                confirmDelete.show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void sendNotesRefresh() {
        if (ActivityManager.getDashboardActivity().currentNoteView != null) {
            ActivityManager.getDashboardActivity().currentNoteView.updateInfo();
        } else {
            setResult(Activity.RESULT_OK);
        }
        finish();
        overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
    }

    @Override
    public void onBackPressed() {
        checkSaved();
    }

    private void checkSaved() {
        if (hasUnsavedChanges) {
            new AlertDialog.Builder(NoteDetailsActivity.this)
                    .setTitle("Draftpad")
                    .setMessage("Do you really want to leave without saving?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            exitActivity();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
        } else {
            exitActivity();
        }
    }

    private void exitActivity() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
    }

    private void saveNote(final Note note, boolean isRetry) {
        note.title = txtTitle.getText().toString();
        note.body = txtBody.getText().toString();
        note.edited = new Date();
        if (ActivityManager.getNoteManager().isChildNote(note)) {
            ActivityManager.getNoteManager().getParentNote(note).edited = note.edited;
            ActivityManager.getNoteManager().hideAllChildNotes();
            ActivityManager.getNoteManager().sortNotes();
        }
        if (note.save()) {
            if (isNewNote) {
                ActivityManager.getNoteManager().addNote(note);
            }
            if (!isRetry) {
                hasUnsavedChanges = false;
                sendNotesRefresh();
            }
        } else {
            Snackbar.make(findViewById(R.id.Dash_CoordinatorLayout),
                    R.string.snackbar_failedToSave, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveNote(note, true);
                        }
                    })
                    .show();
        }
    }

}
