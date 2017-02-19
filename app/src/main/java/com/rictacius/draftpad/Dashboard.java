package com.rictacius.draftpad;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
    RecyclerView notesRecycler;
    LinearLayoutManager notesLayoutManager;
    NotesRecyclerAdapter notesRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.Dash_CreateNoteFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(findViewById(CoordinatorLayout.generateViewId()), "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        notesRecycler = (RecyclerView) findViewById(R.id.Dash_notesRecycler);
        notesLayoutManager = new LinearLayoutManager(this);
        notesRecycler.setLayoutManager(notesLayoutManager);
        notesRecyclerAdapter = new NotesRecyclerAdapter(NoteManager.createTestData());
        notesRecycler.setAdapter(notesRecyclerAdapter);
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
            }

            void updateInfo() {
                noteTitle.setText(note.title);
                String preview = note.text.length() > 20 ? note.text.substring(0, 17) + "..." : note.text;
                notePreview.setText(preview);
                SimpleDateFormat format = new SimpleDateFormat("EEE");
                noteUpdateDay.setText(format.format(note.updated));
                format = new SimpleDateFormat("MMM");
                noteUpdateMonth.setText(format.format(note.updated));
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
            Note note = NoteManager.notes.get(i);
            noteViewHolder.note = note;
            noteViewHolder.updateInfo();
        }

        /*
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
        */

    }
}
