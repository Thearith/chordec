package com.example.chordec.chordec;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chordec.chordec.Database.Chord;
import com.example.chordec.chordec.Database.Database;
import com.example.chordec.chordec.Helper.Constants;
import com.example.chordec.chordec.ListView.CustomAdapter;



public class DatabaseActivity extends ActionBarActivity {

    private static final String TAG = DatabaseActivity.class.getSimpleName();

    private Database database;

    private ListView listView;
    private CustomAdapter adapter;

    private TextView itemsText;

    /*
    * Overriden methods
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        initializeDatabase();
        initializeAdapter();
        initializeListView();
        initializeTextView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_database, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_delete) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
    * Initialize methods
    * */

    private void initializeDatabase() {
        database = new Database(this);
    }

    private void initializeAdapter() {
        adapter = new CustomAdapter(this, R.layout.activity_database,
                database.getChords());
    }

    private void initializeListView() {
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView chordIDTextView = (TextView) view.findViewById(R.id.chordID);
                String chordID = chordIDTextView.getText().toString();

                goToPlayActivity(chordID);
            }
        });


        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {

                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(checkedCount + " Selected");
                adapter.toggleSelection(position);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_delete:

                        final SparseBooleanArray selected = adapter
                                .getSelectedIds();
                        String itemText = selected.size() > 1 ?
                                "items" : "item";

                        //create confirmation dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(DatabaseActivity.this);
                        builder.setTitle("Delete Confirmation")
                               .setIcon(android.R.drawable.ic_dialog_alert)
                                .setCancelable(false)
                                .setMessage("Are you sure you wanted to delete " +
                                    selected.size() + " " + itemText + "?")
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        for (int i = (selected.size() - 1); i >= 0; i--) {
                                            if (selected.valueAt(i)) {
                                                Chord chord = adapter
                                                        .getItem(selected.keyAt(i));

                                                database.deleteChord(chord.getChordID());
                                                adapter.remove(chord);
                                            }
                                        }

                                        initializeTextView();
                                    }
                                });

                            builder.create().show();


                        mode.finish();
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_database, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

    }

    private void initializeTextView() {

        int duration = database.getSumDurations();
        String durationString = Constants.getDurationFormat(duration);

        String records = database.getNumChords() > 1 ?
                database.getNumChords() + " records" :
                database.getNumChords() + " record";

        String durations = duration > (Constants.MILLISECONDS_RATE * Constants.MINUTES_RATE) ?
                durationString + " minutes" :
                durationString + " minute";

        itemsText = (TextView) findViewById(R.id.itemsText);
        itemsText.setText(records + " " + durations);
    }

    private void goToPlayActivity(String chordID) {
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra(Constants.CHORD_ID, chordID);

        startActivity(intent);
    }


 }
