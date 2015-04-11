package com.example.chordec.chordec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easyandroidanimations.library.AnimationListener;
import com.easyandroidanimations.library.FadeInAnimation;
import com.easyandroidanimations.library.FadeOutAnimation;
import com.easyandroidanimations.library.RotationAnimation;
import com.example.chordec.chordec.CSurfaceView.SoundCSurfaceView;
import com.example.chordec.chordec.Database.Chord;
import com.example.chordec.chordec.Database.Database;
import com.example.chordec.chordec.Helper.Constants;
import com.example.chordec.chordec.SoundSampler.SoundSampler;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Date;


public class MainActivity extends Activity
    implements View.OnClickListener{

    // TAG
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String APP_FILE_NAME = "CHORDEC";
    private static final String FILE_EXTENSION = ".pcm";

    // Constants
    private static final int ROTATION_DURATION = 1500;
    private static final int TRANSLATE_DURATION = 1000;
    private static final int FADE_DURATION = 1000;


    // widgets in activity_main.xml
    private ImageButton recordButton;
    private ImageButton pauseButton;
    private ImageButton stopButton;
    //public SoundCSurfaceView surfaceView;


    // layouts in activity_main.xml
    private RelativeLayout recordLayout;


    // database
    private static Database database;

    //sound sampling
    private SoundSampler soundSampler;
    public  short[]  buffer;
    public  int      bufferSize = 1024;
    private String   filePath;

    private int      duration = 0;


    //dimensions and positioning
    private int screenWidth;
    private int screenHeight;
    private int translateY;


    // states
    boolean isRecordLayoutVisible = false;
    boolean isPause = false;

    /*
        overridden methods - crucial for the activity class
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeState();

        initializeWidgets();

        initializeLayout();

        initializeRecorder();

        initializePositioning();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_database) {
            goToDatabase();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void goToDatabase() {
        Intent intent = new Intent(this, DatabaseActivity.class);
        startActivity(intent);
    }

    /*
        initialize functions
    */

    private void initializeState() {
        isRecordLayoutVisible = false;
        isPause = false;
    }

    private void initializeWidgets() {
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(this);

        pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(this);
        pauseButton.setBackgroundResource(R.drawable.pause);

        stopButton = (ImageButton) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(this);
    }


    private void initializeLayout() {
        recordLayout = (RelativeLayout) findViewById(R.id.recordLayout);
    }

    private void initializeRecorder() {
        initializeFile();
        initializeSoundSampler();
    }

    private void initializePositioning() {

        Point size = getScreenDimension();

        screenWidth = size.x;
        screenHeight = size.y;

        translateY = (int) (screenHeight - (
                getResources().getDimension(R.dimen.record_layout_height) * 5 / 6.0 +
                        getResources().getDimension(R.dimen.record_button_height) +
                        getResources().getDimension(R.dimen.record_button_margin_top)));

        Log.d(TAG, "translateY = " + translateY);
    }

    private void initializeFile() {
        Database database = new Database(this);
        int nextCardID = database.getNextChordId();

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + APP_FILE_NAME + "-" + nextCardID + FILE_EXTENSION;
    }

    private void initializeSoundSampler(){

        try {
            soundSampler = new SoundSampler(this, filePath);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Cannot instantiate SoundSampler", Toast.LENGTH_LONG).show();
        }

        try {
            soundSampler.init();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Cannot initialize SoundSampler.", Toast.LENGTH_LONG).show();
        }
    }

    /*
        event handlers
    */

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.recordButton:
                isRecordLayoutVisible = !isRecordLayoutVisible;
                animateRecordButton();

                break;

            case R.id.pauseButton:
                if(isRecordLayoutVisible) {
                    changePauseButtonSrc();
                }

                break;

            case R.id.stopButton:
                if(isRecordLayoutVisible) {
                    stopRecording();
                }
                break;

            default:
                Log.e(TAG, "Widget is not recognized");
                break;
        }
    }

    /*
    * for record button
    * */

     private void animateRecordButton() {
        bounceAnimation();
    }

    private void bounceAnimation() {
        new RotationAnimation(recordButton)
                .setDuration(ROTATION_DURATION)
                .setListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(com.easyandroidanimations.library.Animation animation) {
                        if (isRecordLayoutVisible)
                            transitionDownRecordButton();
                        else
                            transitionUpRecordButton();
                    }
                })
                .animate();
    }

    private void transitionUpRecordButton() {
        transitionRecordButton(0, 0, 0, -translateY);
    }

    private void transitionDownRecordButton() {
        transitionRecordButton(0, 0, 0, translateY);
    }

    private void transitionRecordButton(int startX, int stopX, int startY, final int stopY) {
        Animation animation = new TranslateAnimation(startX, stopX, startY, stopY);
        animation.setDuration(TRANSLATE_DURATION);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                recordButton.clearAnimation();
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        recordButton.getWidth(), recordButton.getHeight());

                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);


                if (stopY > 0) { //move down
                    int marginBottom =
                            (int)getResources().getDimension(R.dimen.record_layout_height)/2
                            - (int)getResources().getDimension(R.dimen.record_button_height)/2;

                    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    lp.setMargins(0, 0, 0, marginBottom);
                } else { //move up
                    lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    lp.setMargins(0,
                            (int) getResources().getDimension(R.dimen.record_button_margin_top), 0, 0);
                }

                recordButton.setLayoutParams(lp);
                changeRecordLayout();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        recordButton.startAnimation(animation);
        recordButton.bringToFront();
    }

    private void changeRecordLayout() {
        if(isRecordLayoutVisible)
            setRecordLayoutVisible();
        else
            setRecordLayoutInvisible();
    }

    private void setRecordLayoutVisible() {
        new FadeInAnimation(recordLayout).setDuration(FADE_DURATION).animate();
    }

    private void setRecordLayoutInvisible() {
        new FadeOutAnimation(recordLayout).setDuration(FADE_DURATION).animate();
    }


    /*
    * for Pause button
    * */

    private void changePauseButtonSrc() {
        isPause = !isPause;
        if(isPause) {
            pauseButton.setBackgroundResource(R.drawable.play);
        } else {
            pauseButton.setBackgroundResource(R.drawable.pause);
        }
    }

    /*
    * for stop button
    * */

    private void stopRecording () {
        stopSampler();
        saveChord();

        // reverting to old view
        isRecordLayoutVisible = !isRecordLayoutVisible;
        animateRecordButton();
    }

    private void stopSampler() {
        soundSampler.stop();
    }

    private void saveChord() {
        createSaveDialog();
    }

    private void createSaveDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final Date date = new Date();

        // creating view for dialog

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_ui, null);

        TextView chordDuration = (TextView) view.findViewById(R.id.chordDuration);
        chordDuration.setText(Constants.getDurationFormat(duration));

        TextView chordDate = (TextView) view.findViewById(R.id.chordDate);
        chordDate.setText(Constants.getDateFormat(date.getTime()));

        final MaterialEditText editText = (MaterialEditText) view.findViewById(R.id.editText);
        editText.setMaxCharacters(30);
        editText.setFloatingLabel(1);
        editText.setFloatingLabelText("Record Name");
        editText.setFloatingLabelTextColor(
               getResources().getColor(R.color.edit_text_floating_color));
        editText.setFloatingLabelTextSize(15);


        // configure dialog
        dialog.setCancelable(false)
              .setNegativeButton(android.R.string.cancel,
                      new DialogInterface.OnClickListener() {

                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              dialog.dismiss();
                          }
              })
              .setPositiveButton(android.R.string.ok,
                      new DialogInterface.OnClickListener() {

                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              String name = editText.getText().toString();
                              if(name != "") {
                                  saveToDatabase(name, date);
                                  Toast.makeText(MainActivity.this,
                                          "Chord created", Toast.LENGTH_SHORT).show();
                                  dialog.dismiss();
                              } else {
                                  //TODO: form validation for edit text
                              }
                          }
              });

        dialog.create().show();

    }

    private void saveToDatabase (String name, Date date) {
        Chord chord = saveContent(name, date);
        database.insertChord(chord);
    }

    private Chord saveContent(String name, Date date) {
        Chord chord = new Chord();

        chord.setChordName(name);
        chord.setChordID(database.getNextChordId());
        chord.setChordPath(filePath);
        chord.setChordDate(date.getTime());

        //chord.setChordDate(getDuration());
        //chord.setChordScore();

        return chord;
    }


    /*
        helper functions
    */

    private Point getScreenDimension() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size;
    }


}
