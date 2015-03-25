package com.example.chordec.chordec;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;


public class MainActivity extends ActionBarActivity
    implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    //    widgets in activity_main.xml
    private ImageButton recordButton;
    private ImageButton pauseButton;
    private ImageButton stopButton;

    //    layouts in activity_main.xml
    private RelativeLayout recordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize widgets
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(this);

        pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(this);

        stopButton = (ImageButton) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(this);

        // initialize layouts
        recordLayout = (RelativeLayout) findViewById(R.id.recordLayout);

    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.recordButton:

                break;

            case R.id.pauseButton:

                break;

            case R.id.stopButton:

                break;

            default:
                Log.e(TAG, "Widget is not recognized");
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_database) {
            goToDatabase();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToDatabase() {

    }
}
