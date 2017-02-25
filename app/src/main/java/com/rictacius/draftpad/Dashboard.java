package com.rictacius.draftpad;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class Dashboard extends AppCompatActivity {
    public static Dashboard instance;
    RecyclerView notesRecycler;
    LinearLayoutManager notesLayoutManager;
    NotesRecyclerAdapter notesRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        instance = this;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.Dash_CreateNoteFAB);
        notesRecycler = (RecyclerView) findViewById(R.id.Dash_notesRecycler);
        notesRecyclerAdapter = new NotesRecyclerAdapter(NoteManager.notes);
        notesLayoutManager = new LinearLayoutManager(this);
        notesRecycler.setLayoutManager(notesLayoutManager);
        notesRecycler.setItemAnimator(new DefaultItemAnimator());
        notesRecycler.setAdapter(notesRecyclerAdapter);
        NoteManager.loadNotes();
        if (NoteManager.notes.size() == 0) {
            NoteManager.createTestData();
        }
        if (NoteManager.notes.size() > 0) {
            findViewById(R.id.Dash_noNotesLabel).setVisibility(View.GONE);
        }
    }

    public void onClickFAB(View view) {
        Intent intent = new Intent(instance, NoteDetailsActivity.class);
        intent.putExtra("NOTE_ID", "");
        instance.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class NotesRecyclerAdapter extends RecyclerView.Adapter<NotesRecyclerAdapter.NoteViewHolder> {

        public static class NoteViewHolder extends RecyclerView.ViewHolder {
            Note note;
            CardView cv;
            TextView noteTitle;
            TextView notePreview;
            TextView noteUpdateDay;
            TextView noteUpdateMonth;

            NoteViewHolder(View itemView) {
                super(itemView);
                cv = (CardView) itemView.findViewById(R.id.cv);
                noteTitle = (TextView) itemView.findViewById(R.id.noteTitle);
                notePreview = (TextView) itemView.findViewById(R.id.notePreview);
                noteUpdateDay = (TextView) itemView.findViewById(R.id.noteUpdateDay);
                noteUpdateMonth = (TextView) itemView.findViewById(R.id.noteUpdateMonth);
                cv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(instance, NoteDetailsActivity.class);
                        intent.putExtra("NOTE_ID", note.id.toString());
                        instance.startActivity(intent);
                    }
                });
            }

            void updateInfo() {
                noteTitle.setText(note.title);
                String preview = note.body.length() > 20 ? note.body.substring(0, 17) + "..." : note.body;
                notePreview.setText(preview);
                SimpleDateFormat format = new SimpleDateFormat("EEE");
                noteUpdateDay.setText(format.format(note.edited));
                format = new SimpleDateFormat("MMM");
                noteUpdateMonth.setText(format.format(note.edited));
            }
        }

        List<Note> notes;

        NotesRecyclerAdapter(List<Note> notes) {
            this.notes = notes;
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        @Override
        public NoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.note_list_row, viewGroup, false);
            NoteViewHolder pvh = new NoteViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(NoteViewHolder noteViewHolder, int i) {
            Note note = notes.get(i);
            noteViewHolder.note = note;
            noteViewHolder.updateInfo();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }
}
