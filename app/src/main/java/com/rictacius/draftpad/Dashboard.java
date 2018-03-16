package com.rictacius.draftpad;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class Dashboard extends AppCompatActivity {
    static DashboardClass dash;
    RecyclerView notesRecycler;
    LinearLayoutManager notesLayoutManager;
    NotesRecyclerAdapter notesRecyclerAdapter;

    public class DashboardClass {
        Context context;
        NoteManager noteManager;
        NotesRecyclerAdapter.NoteViewHolder currentNoteView;

        static final int NOTE_DETAILS_RETURN = 0;

        DashboardClass(Dashboard instance) {
            context = instance;
            noteManager = new NoteManager();
        }

        Activity activity() {
            return (Activity) context;
        }

        NotesRecyclerAdapter notesRecyclerAdapter() {
            return notesRecyclerAdapter;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ActivityManager.initActivity(this);
        ActivityManager.AppBar.init(this, ActivityManager.AppBar.VisibilityToken.SHOW_MORE);

        findViewById(R.id.appbar_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(dash.context, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
            }
        });

        dash = new DashboardClass(this);
        FloatingActionButton fab = findViewById(R.id.Dash_CreateNoteFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNote(Dashboard.dash.context, "");
            }
        });

        notesRecycler = findViewById(R.id.Dash_notesRecycler);
        notesRecyclerAdapter = new NotesRecyclerAdapter(dash.noteManager.notes, this);
        notesLayoutManager = new LinearLayoutManager(this);
        notesRecycler.setLayoutManager(notesLayoutManager);
        notesRecycler.setItemAnimator(new DefaultItemAnimator());
        notesRecycler.setAdapter(notesRecyclerAdapter);
        dash.noteManager.loadNotes();
        if (dash.noteManager.notes.size() > 0) {
            findViewById(R.id.Dash_noNotesLabel).setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (DashboardClass.NOTE_DETAILS_RETURN): {
                if (resultCode == Activity.RESULT_OK) {
                    refreshNoteList();
                }
                break;
            }
        }
    }

    static void refreshNoteList() {
        dash.noteManager.loadNotes();
    }

    static View findView(int id) {
        return dash.activity().findViewById(id);
    }

    static void showSnackBar(String text, int length, int textColor) {
        Snackbar snackbar = Snackbar
                .make(findView(R.id.Dash_CoordinatorLayout), text, length)
                .setActionTextColor(textColor);
        snackbar.show();
    }

    static void showSnackBarWithAction(String text, int length, int textColor, String actionText, final Runnable action) {
        Snackbar snackbar = Snackbar
                .make(findView(R.id.Dash_CoordinatorLayout), text, length)
                .setActionTextColor(textColor)
                .setAction(actionText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        action.run();
                    }
                });
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    public boolean onAppbarMoreClick(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static void openNote(Context context, String noteID) {
        if (noteID.equals("")) {
            dash.currentNoteView = null;
        }
        if (context instanceof Service) {
            Intent dialogIntent = new Intent(context, NoteDetailsActivity.class);
            dialogIntent.putExtra("NOTE_ID", noteID);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(dialogIntent);
        } else {
            Intent intent = new Intent(context, NoteDetailsActivity.class);
            intent.putExtra("NOTE_ID", noteID);
            ((Activity) context).startActivityForResult(intent, DashboardClass.NOTE_DETAILS_RETURN);
            ((Activity) context).overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
        }
    }

    static class NotesRecyclerAdapter extends RecyclerView.Adapter<NotesRecyclerAdapter.NoteViewHolder> {

        static class NoteViewHolder extends RecyclerView.ViewHolder {
            NoteViewHolder viewHolder;
            Note note;
            CardView cv;
            TextView noteTitle;
            TextView notePreview;
            TextView noteUpdateDay;
            TextView noteUpdateMonth;

            NoteViewHolder(final View itemView) {
                super(itemView);
                viewHolder = this;
                cv = itemView.findViewById(R.id.cv);
                noteTitle = itemView.findViewById(R.id.noteTitle);
                notePreview = itemView.findViewById(R.id.notePreview);
                noteUpdateDay = itemView.findViewById(R.id.noteUpdateDay);
                noteUpdateMonth = itemView.findViewById(R.id.noteUpdateMonth);
                final Context context = itemView.getContext();
                cv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dashboard.dash.currentNoteView = viewHolder;
                        Dashboard.openNote(context, note.id.toString());
                    }
                });
            }

            void updateInfo() {
                noteTitle.setText(note.title);
                String preview = note.body.length() > 20 ? note.body.substring(0, 17) + "..." : note.body;
                preview = preview.replaceAll("\\n", "");
                notePreview.setText(preview);
                SimpleDateFormat format = new SimpleDateFormat("dd");
                noteUpdateDay.setText(format.format(note.edited));
                format = new SimpleDateFormat("MMM");
                noteUpdateMonth.setText(format.format(note.edited));
            }
        }

        private Context context;
        List<Note> notes;
        private int lastPosition = -1;

        NotesRecyclerAdapter(List<Note> notes, Context context) {
            this.notes = notes;
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        @Override
        public NoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.note_list_row, viewGroup, false);
            return new NoteViewHolder(v);
        }

        @Override
        public void onBindViewHolder(NoteViewHolder noteViewHolder, int i) {
            noteViewHolder.note = notes.get(i);
            noteViewHolder.updateInfo();
            setAnimation(noteViewHolder.itemView, i);
        }

        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.trans_fade_in);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }
}
