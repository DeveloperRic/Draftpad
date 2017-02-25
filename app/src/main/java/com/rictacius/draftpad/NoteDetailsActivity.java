package com.rictacius.draftpad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.UUID;

public class NoteDetailsActivity extends AppCompatActivity {
    Note note;
    EditText txtTitle;
    EditText txtBody;
    TextView lblDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        txtTitle = (EditText) findViewById(R.id.ND_txtTitle);
        txtBody = (EditText) findViewById(R.id.ND_txtBody);
        lblDone = (TextView) findViewById(R.id.ND_lblDone);

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
    }
}
