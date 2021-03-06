package com.rictacius.draftpad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Dashboard extends AppCompatActivity {
    RecyclerView notesRecycler;
    LinearLayoutManager notesLayoutManager;
    NotesRecyclerAdapter notesRecyclerAdapter;

    NotesRecyclerAdapter.NoteViewHolder currentNoteView;

    static final int NOTE_DETAILS_RETURN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ActivityManager.setDashboardClass(this);

        ErrorCode initResult = ActivityManager.init(this);
        if (initResult != ErrorCode.NO_ERROR) {
            findViewById(R.id.Dash_notLoadedLabel).setVisibility(View.VISIBLE);
            findViewById(R.id.Dash_noNotesLabel).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.Dash_errorCode))
                    .setText(String.format(getString(R.string.dash_errorCode), initResult.getCode()));
            return;
        }

        setSupportActionBar((Toolbar) findViewById(R.id.dash_appbar));

        findViewById(R.id.dash_fabCreateNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNote("");
            }
        });
        findViewById(R.id.dash_fabNewGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note note = ActivityManager.getNoteManager().createNewNote();
                note.title = getResources().getString(R.string.dash_newGroupTitle);
                Note child = ActivityManager.getNoteManager().createNewNote();
                child.title = getResources().getString(R.string.dash_newGroupChildTitle);
                child.body = getResources().getString(R.string.dash_newGroupChildBody);
                note.children.add(child);
                note.isNoteGroup = true;
                if (child.save() && note.save()) {
                    ActivityManager.getNoteManager().addNote(note);
                } else {
                    Snackbar.make(findViewById(R.id.Dash_CoordinatorLayout),
                            R.string.snackbar_failedToMakeGroup, Snackbar.LENGTH_INDEFINITE)
                            .show();
                }
            }
        });

        notesRecycler = findViewById(R.id.Dash_notesRecycler);
        notesRecyclerAdapter = new NotesRecyclerAdapter(ActivityManager.getNoteManager().notes);
        notesLayoutManager = new LinearLayoutManager(this);
        notesRecycler.setLayoutManager(notesLayoutManager);
        notesRecycler.setItemAnimator(new DefaultItemAnimator());
        notesRecycler.setAdapter(notesRecyclerAdapter);

        if (ActivityManager.getSettings().sortNotes) {
            ActivityManager.getNoteManager().sortNotes();
        }

        if (ActivityManager.getNoteManager().notesLoaded) {
            if (ActivityManager.getNoteManager().notes.size() > 0) {
                ActivityManager.getNoteManager().toggleNoNotesLabel(false);
            }
        } else {
            findViewById(R.id.Dash_notLoadedLabel).setVisibility(View.VISIBLE);
            ActivityManager.getNoteManager().toggleNoNotesLabel(false);
            Snackbar.make(findViewById(R.id.Dash_CoordinatorLayout),
                    R.string.snackbar_failedToLoad, Snackbar.LENGTH_INDEFINITE)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);


        MenuItem searchItem = menu.findItem(R.id.dash_appbar_action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ActivityManager.getNoteManager().filterNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ActivityManager.getNoteManager().filterNotes(newText);
                return true;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                if (ActivityManager.getNoteManager().notesSorted) {
                    ActivityManager.getNoteManager().unsortNotes();
                }
                ActivityManager.getNoteManager().unhideAllChildNotes();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                ActivityManager.getNoteManager().filterNotes("");
                if (ActivityManager.getSettings().sortNotes) {
                    ActivityManager.getNoteManager().sortNotes();
                } else {
                    ActivityManager.getNoteManager().unsortNotes();
                }
                ActivityManager.getNoteManager().hideAllChildNotes();
                return true;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dash_appbar_action_sort:
                ActivityManager.getNoteManager().hideAllChildNotes();
                if (ActivityManager.getNoteManager().notesSorted) {
                    ActivityManager.getNoteManager().unsortNotes();
                    Toast.makeText(this, R.string.appbar_result_unsorted, Toast.LENGTH_SHORT).show();
                } else {
                    ActivityManager.getNoteManager().sortNotes();
                    Toast.makeText(this, R.string.appbar_result_sorted, Toast.LENGTH_SHORT).show();
                }
                ActivityManager.getSettings().toggleSortNotes();
                ActivityManager.requestSave();
                return true;

            case R.id.dash_appbar_action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void openNote(String noteID) {
        if (noteID.equals("")) {
            currentNoteView = null;
        }
        Intent intent = new Intent(this, NoteDetailsActivity.class);
        intent.putExtra("NOTE_ID", noteID);
        startActivityForResult(intent, NOTE_DETAILS_RETURN);
        overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
    }

    void notifyFailedToInit(final String ID_String) {
        Snackbar.make(findViewById(R.id.Dash_CoordinatorLayout),
                R.string.snackbar_failedToInitialise, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityManager.getDashboardActivity().openNote(ID_String);
                    }
                })
                .show();
    }

    void notifyNoteDeleted(final Handler deleteHandler, final Runnable deleteRunnable, final Note note, final int position) {
        Snackbar.make(findViewById(R.id.Dash_CoordinatorLayout),
                R.string.snackbar_noteDeleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteHandler.removeCallbacks(deleteRunnable);
                        ActivityManager.getNoteManager().toggleNoNotesLabel(false);
                        ActivityManager.getNoteManager().unhideNote(note, position);
                    }
                })
                .show();
    }

    void notifyNoteNotDeleted(final Note note) {
        Snackbar.make(findViewById(R.id.Dash_CoordinatorLayout),
                R.string.snackbar_noteNotDeleted, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!ActivityManager.getNoteManager().deleteNote(note)) {
                            Snackbar.make(findViewById(R.id.Dash_CoordinatorLayout),
                                    R.string.snackbar_noteNotDeleted, Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    }
                })
                .show();
    }

    static class NotesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        static class NoteViewHolder extends RecyclerView.ViewHolder {
            Note note;
            NoteViewHolder viewHolder;
            Context context;
            CardView cv;
            TextView noteTitle;
            TextView notePreview;
            ImageButton noteExpand;

            NoteViewHolder(final View itemView) {
                super(itemView);
                viewHolder = this;
                context = itemView.getContext();
                cv = itemView.findViewById(R.id.note_cv);

                noteTitle = itemView.findViewById(R.id.noteTitle);
                notePreview = itemView.findViewById(R.id.notePreview);
                cv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!ActivityManager.getNoteManager().isSelectingChildNotes()) {
                            ActivityManager.getDashboardActivity().currentNoteView = viewHolder;
                            ActivityManager.getDashboardActivity().openNote(note.id.toString());
                        } else if (!note.isNoteGroup) {
                            ActivityManager.getNoteManager().toggleNoteSelectionState(note);
                            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(context,
                                    R.string.dash_cannotSelectParentAsChild, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                cv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (ActivityManager.getNoteManager().isSelectingChildNotes()) {
                            if (ActivityManager.getNoteManager().isSelectedParentNote(note)) {
                                ActivityManager.getNoteManager().stopNoteGrouping();
                                ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyDataSetChanged();
                                Snackbar.make(ActivityManager.getDashboardActivity().findViewById(R.id.Dash_CoordinatorLayout),
                                        R.string.dash_noLongerSelectingChildren, Snackbar.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context,
                                        R.string.dash_alreadySelectingChildren, Toast.LENGTH_SHORT).show();
                            }
                        } else if (!ActivityManager.getNoteManager().isChildNote(note)) {
                            ActivityManager.getNoteManager().startNoteGrouping(note);
                            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyDataSetChanged();

                            Snackbar.make(ActivityManager.getDashboardActivity().findViewById(R.id.Dash_CoordinatorLayout),
                                    R.string.dash_selectingChildrenSnackbar, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.snackbar_finish, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            ActivityManager.getNoteManager().stopNoteGrouping();
                                            ActivityManager.getDashboardActivity().notesRecyclerAdapter.notifyDataSetChanged();
                                            Snackbar.make(ActivityManager.getDashboardActivity().findViewById(R.id.Dash_CoordinatorLayout),
                                                    R.string.dash_noLongerSelectingChildren, Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .show();
                        } else {
                            Toast.makeText(context,
                                    R.string.dash_cannotSelectChildAsParent, Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });

                noteExpand = itemView.findViewById(R.id.noteExpand);
                noteExpand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (note.children.size() != 0) {
                            if (!note.noteChildrenExpanded) {
                                int parentPosition = ActivityManager.getNoteManager().findLocation(note);
                                for (int i = 0; i < note.children.size(); i++) {
                                    Note child = note.children.get(i);
                                    ActivityManager.getNoteManager().unhideNote(child, parentPosition + i + 1);
                                }
                                noteExpand.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_expand_less_black_24dp));
                            } else {
                                for (Note child : note.children) {
                                    ActivityManager.getNoteManager().hideNote(child);
                                }
                                noteExpand.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
                            }
                            note.noteChildrenExpanded = !note.noteChildrenExpanded;
                        }
                    }
                });
            }

            void updateInfo() {
                noteTitle.setText(note.title);
                String preview = note.body.length() > 20 ? note.body.substring(0, 17) + "..." : note.body;
                preview = preview.replaceAll("\\n", "\t");
                notePreview.setText(preview);
                if (ActivityManager.getNoteManager().isSelectingChildNotes()) {
                    if (ActivityManager.getNoteManager().isSelectedParentNote(note)) {
                        cv.setCardBackgroundColor(
                                context.getResources().getColor(R.color.colorRowParent));
                    } else if (ActivityManager.getNoteManager().isSelectedChildNote(note)) {
                        cv.setCardBackgroundColor(
                                context.getResources().getColor(R.color.colorRowSelected));
                    } else {
                        cv.setCardBackgroundColor(
                                context.getResources().getColor(R.color.cardview_light_background));
                    }
                } else {
                    if (ActivityManager.getNoteManager().isChildNote(note)) {
                        cv.setCardBackgroundColor(
                                context.getResources().getColor(R.color.colorRowChild));
                    } else {
                        cv.setCardBackgroundColor(
                                context.getResources().getColor(R.color.cardview_light_background));
                    }
                }
                noteExpand.setVisibility(note.children.size() != 0 && !ActivityManager.getNoteManager().isSelectingChildNotes() ? View.VISIBLE : View.GONE);
                if (note.children.size() != 0) {
                    if (note.noteChildrenExpanded) {
                        noteExpand.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_expand_less_black_24dp));
                    } else {
                        noteExpand.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
                    }
                }
            }
        }

        static class DateViewHolder extends RecyclerView.ViewHolder {
            Note.NoteDateInfo dateInfo;
            TextView noteDateInfoText;

            DateViewHolder(View itemView) {
                super(itemView);

                noteDateInfoText = itemView.findViewById(R.id.noteDateInfoText);
            }

            void updateInfo() {
                if (dateInfo.date != null) {
                    noteDateInfoText.setText(dateInfo.formatDate());
                } else {
                    noteDateInfoText.setText(dateInfo.infoResID);
                }
            }
        }

        List<Note> notes;
        private int lastPosition = -1;

        NotesRecyclerAdapter(List<Note> notes) {
            this.notes = notes;
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (notes.get(position) instanceof Note.NoteDateInfo) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            switch (viewType) {
                case 0:
                    return new NoteViewHolder(LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.dashlist_note_row, viewGroup, false));
                case 1:
                    return new DateViewHolder(LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.dashlist_date_row, viewGroup, false));
            }
            return null;
        }

        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            switch (viewHolder.getItemViewType()) {
                case 0:
                    NoteViewHolder noteViewHolder = (NoteViewHolder) viewHolder;
                    noteViewHolder.note = notes.get(position);
                    noteViewHolder.updateInfo();
                    break;
                case 1:
                    DateViewHolder dateViewHolder = (DateViewHolder) viewHolder;
                    dateViewHolder.dateInfo = (Note.NoteDateInfo) notes.get(position);
                    dateViewHolder.updateInfo();
            }
            setAnimation(viewHolder.itemView, position);
        }

        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(
                        ActivityManager.getDashboardActivity(), R.anim.trans_fade_in);
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
