package com.example.chordec.chordec;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chordec.chordec.Database.Database;
import com.example.chordec.chordec.Helper.Constants;
import com.example.chordec.chordec.ListView.CustomAdapter;


public class DatabaseActivity extends Activity {


    private Database database;

    private ListView listView;
    private CustomAdapter adapter;

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_database, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

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
    }

    private void goToPlayActivity(String chordID) {
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra(Constants.CHORD_ID, chordID);

        startActivity(intent);
    }



 }
