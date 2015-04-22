package com.example.chordec.chordec;

/**
 * Created by thearith on 4/4/15.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easyandroidanimations.library.AnimationListener;
import com.easyandroidanimations.library.BounceAnimation;
import com.easyandroidanimations.library.FadeInAnimation;
import com.easyandroidanimations.library.FadeOutAnimation;
import com.easyandroidanimations.library.RotationAnimation;

import com.example.chordec.chordec.Database.Chord;
import com.example.chordec.chordec.Database.Database;
import com.example.chordec.chordec.Helper.Constants;
import com.example.chordec.chordec.TarsosDSP.SpectralInfo;

import com.example.chordec.chordec.TarsosDSP.AudioDispatcherFactory;
import com.example.chordec.chordec.TarsosDSP.SpectralPeakProcessor;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;


public class MainActivity extends ActionBarActivity
    implements View.OnClickListener{

    // TAG
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String APP_FILE_NAME = "CHORDEC";
    private static final String FILE_EXTENSION = ".wav";

    // Constants
    private static final int ROTATION_DURATION = 1500;
    private static final int TRANSLATE_DURATION = 1000;
    private static final int FADE_DURATION = 1000;
    private static final int PULSE_DURATION = 800;

    // audio sampling constants
    private static final int  SAMPLING_FREQ = 44100;     // sampling frequency
    private static final int FFT_LEN = 4096;
    private static final int STEP_SIZE = 512;

    private double[] N_FREQ =
            { 61.735,65.406, 69.296, 73.416,77.782,82.407,87.307,92.499,97.999,103.826,110,116.541,123.471, 130.813 }; // actual frequency of bass note in kHz

    // widgets in activity_main.xml
    private ImageButton recordButton;
    private ImageButton pauseButton;
    private ImageButton stopButton;
    private ImageView   recordingImage;
    private TextView    timerTextView;

    private TextView    hintText;
    private ImageView   hintImage;
    private TextView    hintText2;

    private TextView    hintChordText;
    private TextView    chordText;
    private TextView    pitchText;

    // layouts in activity_main.xml
    private RelativeLayout recordLayout;
    private LinearLayout timerLayout;

    // database
    private static Database database;

    //Dispatcher thread
    private AudioDispatcher dispatcher;
    private Thread dispatcherThread;

    // Tarsos DSP variables
    private char prevChord = 0;
    private char currChord = 0;
    private String chordProgression;

    List<SpectralInfo> spectalInfo;


    // timer
    private boolean isTimerRunning;
    private Timer timer;
    private TimerTask timerTask;
    private int   duration;

    //dimensions and positioning
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

        customizeActionBar();

        initializeDatabase();

        initializeState();

        initializeSpectralInfo();

        initializeTimer();

        initializeWidgets();

        initializeLayout();

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
            goToDatabaseActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void customizeActionBar() {
        ActionBar menu = getSupportActionBar();
        menu.setIcon(R.drawable.app_logo);
        menu.setLogo(R.drawable.app_logo);
        menu.setDisplayUseLogoEnabled(true);
    }

    private void goToDatabaseActivity() {
        if(!isRecordLayoutVisible) {
            Intent intent = new Intent(this, DatabaseActivity.class);
            startActivity(intent);
        }
    }

    /*
        initialize functions
    */

    private void initializeDatabase() {
        database = new Database(this);
    }

    private void initializeState() {
        isRecordLayoutVisible = false;
        isPause = false;
    }

    private void initializeTimer() {

        isTimerRunning = false;
        timer = new Timer();
        duration = 0;

        timerTask = new TimerTask() {

            @Override
            public void run() {

                if(isTimerRunning) {
                    duration += Constants.MILLISECONDS_RATE;
                    timerTextView.post(new Runnable() {
                        public void run() {
                            timerTextView.setText(
                                    Constants.getDurationFormat(duration));
                        }
                    });
                    recordingImage.post(new Runnable() {
                        public void run() {
                            new BounceAnimation(recordingImage).setNumOfBounces(1)
                                    .setDuration(PULSE_DURATION).animate();
                        }
                    });
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, Constants.MILLISECONDS_RATE);
    }

    private void initializeSpectralInfo() {
        spectalInfo = new ArrayList<SpectralInfo>();
    }

    private void initializeWidgets() {
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(this);

        pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(this);
        pauseButton.setBackgroundResource(R.drawable.pause);

        stopButton = (ImageButton) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(this);

        timerTextView = (TextView) findViewById(R.id.timerTextView);

        hintText = (TextView) findViewById(R.id.hintText);
        hintImage = (ImageView) findViewById(R.id.hintImage);
        hintText2 = (TextView) findViewById(R.id.hintText2);

        recordingImage = (ImageView) findViewById(R.id.recordingImage);

        hintChordText = (TextView) findViewById(R.id.hintChordText);
        chordText = (TextView) findViewById(R.id.chordText);
        pitchText = (TextView) findViewById(R.id.pitchText);
    }


    private void initializeLayout() {
        recordLayout = (RelativeLayout) findViewById(R.id.recordLayout);
        timerLayout = (LinearLayout) findViewById(R.id.timerLayout);
    }

    private void initializeRecorder() {
        initializeSpectralPeakDetector();
    }

    private void initializePositioning() {

        Point size = getScreenDimension();

        screenHeight = size.y;

        translateY = (int) (screenHeight - (
                getResources().getDimension(R.dimen.record_layout_height) * 5 / 6.0 +
                        getResources().getDimension(R.dimen.record_button_height) +
                        getResources().getDimension(R.dimen.record_button_margin_top)));

        Log.d(TAG, "translateY = " + translateY);
    }

    private void initializeFile() {
//        String nextCardID = database.getStringNextChordId();
//
//        filePath = Environment.getExternalStorageDirectory().
//                getAbsolutePath() +
//                "/" + APP_FILE_NAME + nextCardID + FILE_EXTENSION;
//
//        File directory = new File(filePath).getParentFile();
//        if (!directory.exists() && !directory.mkdirs()) {
//            Log.e(TAG, "Path to file could not be created.");
//        }
//
//        Log.d(TAG, filePath);
    }

    private void initializeSpectralPeakDetector(){
        int overlap = FFT_LEN - STEP_SIZE;
        overlap = overlap < 1 ? 128 : overlap;

        chordProgression = "";

        final SpectralPeakProcessor spectralPeakFollower = new SpectralPeakProcessor(FFT_LEN, overlap, SAMPLING_FREQ);
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLING_FREQ, FFT_LEN, 0);
        AudioDispatcherFactory.startRecording();
        dispatcher.addAudioProcessor(spectralPeakFollower);

        // add pitch detection
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result,AudioEvent e) {
                final int pitchInHz = (int) result.getPitch();

                if(pitchInHz == 24 || pitchInHz == 49 || pitchInHz == 98) {
                    currChord = 'G';
                } else if (pitchInHz == 16 || pitchInHz == 32 || pitchInHz == 65) {
                    currChord = 'C';
                }else if (pitchInHz == 18 || pitchInHz == 36 || pitchInHz == 73) {
                    currChord = 'D';
                }else if (pitchInHz == 20 || pitchInHz == 41 || pitchInHz == 82) {
                    currChord = 'E';
                }else if (pitchInHz == 22 || pitchInHz == 43 ) {
                    currChord = 'F';
                }else if (pitchInHz == 13 || pitchInHz == 27 || pitchInHz == 54 || pitchInHz == 55) {
                    currChord = 'A';
                }
                if (currChord != prevChord){
                    prevChord = currChord;
                    chordProgression += prevChord + ", ";
                }
                final char displayChord = prevChord;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chordText.setText(String.valueOf(displayChord));
                        if(pitchInHz > 0) {
                            pitchText.setText(pitchInHz + " Hz");
                        }
                        else
                            pitchText.setText("");
                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, SAMPLING_FREQ, FFT_LEN, pdh);
        dispatcher.addAudioProcessor(p);
    }

    /*
        event handlers
    */

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.recordButton:
                if(!isRecordLayoutVisible) {
                    animateRecordButton();
                    initializeBeforeRecording();
                    Log.d(TAG, "after pressing record button");
                }
                isRecordLayoutVisible = true;


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

    /**
     * Initialize before recording
     * */
    private void initializeBeforeRecording() {
        initializeFile();
        initializeRecorder();
        startDispatcherThread();
    }

    private void startDispatcherThread() {
        dispatcherThread = new Thread(dispatcher);
        dispatcherThread.start();
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

                    recordButton.setBackgroundResource(R.drawable.white_bg);
                } else { //move up
                    lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    lp.setMargins(0,
                            (int) getResources().getDimension(R.dimen.record_button_margin_top), 0, 0);

                    recordButton.setBackgroundColor(Color.TRANSPARENT);
                }

                recordButton.setLayoutParams(lp);
                changeLayoutsVisibility();
                changeWidgetsVisibility();
                startTimer();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        recordButton.startAnimation(animation);
        recordButton.bringToFront();
    }

    private void changeLayoutsVisibility() {
        if(isRecordLayoutVisible) {
            setLayoutsVisible();
        } else {
            setLayoutsInvisible();
        }
    }

    private void changeWidgetsVisibility() {
        if(isRecordLayoutVisible) {
            setWidgetsVisible();
        } else {
            setWidgetsInvisible();
        }
    }

    private void setLayoutsVisible() {
        new FadeInAnimation(recordLayout).setDuration(FADE_DURATION).animate();
        new FadeInAnimation(timerLayout).setDuration(FADE_DURATION).animate();
    }

    private void setLayoutsInvisible() {
        new FadeOutAnimation(recordLayout).setDuration(FADE_DURATION).animate();
        new FadeOutAnimation(timerLayout).setDuration(FADE_DURATION).animate();
    }

    private void setWidgetsVisible() {
        new FadeOutAnimation(hintText).setDuration(FADE_DURATION).animate();
        new FadeOutAnimation(hintImage).setDuration(FADE_DURATION).animate();
        new FadeOutAnimation(hintText2).setDuration(FADE_DURATION).animate();

        new FadeInAnimation(recordingImage).setDuration(FADE_DURATION).animate();
        new FadeInAnimation(hintChordText).setDuration(FADE_DURATION).animate();
        new FadeInAnimation(chordText).setDuration(FADE_DURATION).animate();
        new FadeInAnimation(pitchText).setDuration(FADE_DURATION).animate();
    }

    private void setWidgetsInvisible() {
        new FadeInAnimation(hintText).setDuration(FADE_DURATION).animate();
        new FadeInAnimation(hintImage).setDuration(FADE_DURATION).animate();
        new FadeInAnimation(hintText2).setDuration(FADE_DURATION).animate();

        new FadeOutAnimation(recordingImage).setDuration(FADE_DURATION).animate();
        new FadeOutAnimation(hintChordText).setDuration(FADE_DURATION).animate();
        new FadeOutAnimation(chordText).setDuration(FADE_DURATION).animate();
        new FadeOutAnimation(pitchText).setDuration(FADE_DURATION).animate();
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
        saveChord();
    }

    private void saveChord() {
        createSaveDialog();
    }

    private void createSaveDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Date date = Calendar.getInstance().getTime();
        final String dateFormat = Constants.getDateFormat(date.getTime());

        // creating view for dialog

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_ui, null);

        TextView chordDuration = (TextView) view.findViewById(R.id.chordDuration);
        chordDuration.setText(Constants.getDurationFormat(duration));

        TextView chordDate = (TextView) view.findViewById(R.id.chordDate);
        chordDate.setText(dateFormat);

        final MaterialEditText editText = (MaterialEditText) view.findViewById(R.id.editText);
        editText.setPrimaryColor(
                getResources().getColor(R.color.edit_text_floating_color));

        editText.setMaxCharacters(30);
        editText.setFloatingLabel(1);
        editText.setFloatingLabelText("Record Name");
        editText.setFloatingLabelTextColor(
                getResources().getColor(R.color.edit_text_floating_color));
        editText.setFloatingLabelTextSize(30);

        //hide soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);


        // pause timer
        pauseTimer();

        // configure dialog
        builder.setView(view)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == Dialog.BUTTON_NEGATIVE) {

                                    resetAfterRecording();
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

                                        saveMusicRecord();
                                        saveToDatabase(name, dateFormat);

                                        Toast.makeText(MainActivity.this,
                                                "Chord created", Toast.LENGTH_SHORT).show();

                                        resetAfterRecording();
                                        dialog.dismiss();
                                    }
                                }
                            }
                        });
        builder.create().show();

    }

    private void saveToDatabase (String name, String date) {
        Chord chord = saveContent(name, date);
        database.insertChord(chord);
    }

    private Chord saveContent(String name, String date) {
        Chord chord = new Chord();

        String filePath = AudioDispatcherFactory.getFilename();

        chord.setChordName(name);
        chord.setChordID(database.getNextChordId());
        chord.setChordPath(filePath);
        chord.setChordDate(date);
        chord.setChordDuration(duration);

        chord.setChordScore(chordProgression);

        return chord;
    }

    private void saveMusicRecord() {
        AudioDispatcherFactory.stopRecording();
    }

    /**
     * reset after recording
     * */
    private void resetAfterRecording() {
        isRecordLayoutVisible = false;
        animateRecordButton();
        resetDispatcher();
        resetTimer();

    }

    private void resetDispatcher() {
        resetChordString();
        //resetDispatcherThread();
    }

    private void resetChordString() {
        chordProgression = "";
        currChord = prevChord = 0;
    }

    private void resetDispatcherThread() {
        try {
            dispatcherThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    * Recording audio and writing it to file
    * */


    /*
    * Timer utility functions
    * */

    private void startTimer() {
        isTimerRunning = true;
        duration = 0;
    }

    private void pauseTimer() {
        isTimerRunning = false;
    }

    private void resetTimer() {
        duration = 0;
        isTimerRunning = false;
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
