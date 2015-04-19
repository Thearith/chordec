package com.example.chordec.chordec;

/**
 * Created by thearith on 10/4/15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chordec.chordec.CSurfaceView.LineTextView;
import com.example.chordec.chordec.Database.Chord;
import com.example.chordec.chordec.Database.Database;
import com.example.chordec.chordec.Helper.Constants;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;


public class PlayActivity extends ActionBarActivity implements MediaController.MediaPlayerControl {

    private static final String TAG = PlayActivity.class.getSimpleName();

    private Database database;

    private Chord chord;

    // widgets
    private TextView titleTextView;
    private TextView scoreTextView;

    // media player
    private MediaPlayer mMediaPlayer;

    // media controller
    private MediaController mMediaController;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeDatabase();

        initializeData();

        initializeWidgets();


        initializeMedia();
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

        if(id == android.R.id.home) {

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mMediaController.hide();
        mHandler.removeCallbacks(mRunnable);

        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMediaController.hide();
        mHandler.removeCallbacks(mRunnable);

        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
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
        scoreTextView = (LineTextView)findViewById(R.id.scoreTextView);
        scoreTextView.setText(scoreFormat(chord.getChordScore()));
    }

    private void initializeMedia() {
        File file = new File(chord.getChordPath());
        if(file.exists())
            initializeMediaPlayer();
    }

    private void initializeMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaController = new MediaController(this) {
            //for not hiding
            @Override
            public void hide() {}

            //for 'back' key action
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    Activity a = (Activity)getContext();
                    a.finish();
                }
                return true;
            }
        };
        mMediaController.setMediaPlayer(PlayActivity.this);
        mMediaController.setBackgroundResource(R.color.media_controller_bg);
        mMediaController.setAnchorView(findViewById(R.id.audioView));

        try {
            mMediaPlayer.setDataSource(chord.getChordPath());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("PlayAudioDemo", "Could not open file " + chord.getChordPath() + " for playback.", e);
        }

        mRunnable = new Runnable() {
            public void run() {
                mMediaPlayer.start();
                mMediaController.show(900000000);
            }
        };

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mHandler.post(mRunnable);
            }
        });

    }

    private String scoreFormat(String score) {
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

    /*
    * Media player + media controller
    * */

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        if(mMediaPlayer != null) {
            return (mMediaPlayer.getCurrentPosition() * 100) / mMediaPlayer.getDuration();
        }

        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(mMediaPlayer != null)
            return mMediaPlayer.getCurrentPosition();

        return 0;
    }

    @Override
    public int getDuration() {
        if(mMediaPlayer != null)
            return mMediaPlayer.getDuration();

        return 0;
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
        if(mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
    }

    @Override
    public void seekTo(int pos) {
        mMediaPlayer.seekTo(pos);
    }

    @Override
    public void start() {
        mMediaPlayer.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mMediaController.show();

        return false;
    }




}
