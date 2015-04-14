package com.example.chordec.chordec;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chordec.chordec.Database.Chord;
import com.example.chordec.chordec.Database.Database;
import com.example.chordec.chordec.Helper.Constants;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;


public class PlayActivity extends ActionBarActivity {


    private Database database;

    private Chord chord;

    // widgets
    private TextView titleTextView;
    private TextView scoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        initializeDatabase();

        initializeData();

        initializeWidgets();

        //playRecord();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {

            createSaveDialog();

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

    private void initializeData() {
        int chordID = Integer.parseInt(
                getIntent().getStringExtra(Constants.CHORD_ID));
        chord = database.getChord(chordID);
    }

    private void initializeWidgets() {
        initializeChordNameTextView();
        initializeScoreTextView();
    }

    private void initializeChordNameTextView() {
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(chord.getChordName());
    }

    private void initializeScoreTextView() {
        scoreTextView = (TextView)findViewById(R.id.scoreTextView);
        scoreTextView.setText(scoreFormat(chord.getChordScore()));
    }

    private void playRecord(){

        File file = new File(Environment.getExternalStorageDirectory(), chord.getChordPath());

        int shortSizeInBytes = Short.SIZE/Byte.SIZE;

        int bufferSizeInBytes = (int)(file.length()/shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int i = 0;
            while(dataInputStream.available() > 0){
                audioData[i] = dataInputStream.readShort();
                i++;
            }

            dataInputStream.close();

            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    11025,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();
            audioTrack.write(audioData, 0, bufferSizeInBytes);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String scoreFormat(String score) {
        //TODO return score
        return score;
    }

    private void createSaveDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Date date = Calendar.getInstance().getTime();
        final String dateFormat = Constants.getDateFormat(date.getTime());

        // creating view for dialog

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_ui, null);

        TextView chordDuration = (TextView) view.findViewById(R.id.chordDuration);
        chordDuration.setText(Constants.getDurationFormat(chord.getChordDuration()));

        TextView chordDate = (TextView) view.findViewById(R.id.chordDate);
        chordDate.setText(dateFormat);

        final MaterialEditText editText = (MaterialEditText) view.findViewById(R.id.editText);
        editText.setPrimaryColor(
                getResources().getColor(R.color.edit_text_floating_color));
        editText.setText(chord.getChordName());
        editText.setSelection(chord.getChordName().length());

        editText.setMaxCharacters(30);
        editText.setFloatingLabel(1);
        editText.setFloatingLabelText("Record Name");
        editText.setFloatingLabelTextColor(
                getResources().getColor(R.color.edit_text_floating_color));
        editText.setFloatingLabelTextSize(30);

        //hide soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        // configure dialog
        builder.setView(view)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == Dialog.BUTTON_NEGATIVE) {
                                    dialog.dismiss();

                                }
                            }
                        })
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which == Dialog.BUTTON_POSITIVE) {
                                    String name = editText.getText().toString();

                                    if (!name.isEmpty()) {

                                        int result = editDatabase(name);
                                        if(result != 0) {
                                            initializeChordNameTextView();
                                            Toast.makeText(PlayActivity.this,
                                                    "Chord name changed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(PlayActivity.this,
                                                    "Error in changing chord name", Toast.LENGTH_SHORT).show();
                                        }

                                        dialog.dismiss();
                                    }
                                }
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private int editDatabase(String chordName) {
        chord.setChordName(chordName);
        return database.editChord(chord);
    }

}
