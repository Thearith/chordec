package com.example.chordec.chordec;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.easyandroidanimations.library.AnimationListener;
import com.easyandroidanimations.library.FadeInAnimation;
import com.easyandroidanimations.library.FadeOutAnimation;
import com.easyandroidanimations.library.RotationAnimation;
import com.example.chordec.chordec.CSurfaceView.SoundCSurfaceView;
import com.example.chordec.chordec.SoundSampler.SoundSampler;

import java.lang.reflect.Method;


public class MainActivity extends Activity
    implements View.OnClickListener{

    // constants
    private static final String TAG = MainActivity.class.getSimpleName();
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

    //sound sampling
    private SoundSampler soundSampler;
    public  short[]  buffer;
    public  int      bufferSize;

    //dimensions and positioning
    private int screenWidth;
    private int screenHeight;
    private int translateY;

    // states
    boolean isRecordLayoutVisible = false;
    boolean isPause = true;

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

        // get screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        translateY =(int)( screenHeight - (
                getResources().getDimension(R.dimen.record_layout_height)*5/6.0 +
                getResources().getDimension(R.dimen.record_button_height) +
                getResources().getDimension(R.dimen.record_button_margin_top)));
        Log.d(TAG, "translateY = " + translateY);

//        initialize sound sampler
//        try {
//            soundSampler = new SoundSampler(this);
//
//        } catch (Exception e) {
//            Log.e(TAG, "Cannot instantiate SoundSampler");
//        }
//
//        try {
//            soundSampler.init();
//        } catch (Exception e) {
//            Log.e(TAG, "Cannot initialize SoundSampler");
//        }
//
//        surfaceView = (SoundCSurfaceView)findViewById(R.id.csurfaceview);
//        surfaceView.drawThread.setBuffer(buffer);
        //surfaceView.drawFlag = Boolean.valueOf(false);

    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.recordButton:
                isRecordLayoutVisible = !isRecordLayoutVisible;
                animateRecordButton();
                //changeSurfaceViewVisibility();

                break;

            case R.id.pauseButton:
                if(isRecordLayoutVisible) {
                    changePauseButtonSrc();
                    //pauseSurfaceView();
                }

                break;

            case R.id.stopButton:

                break;

            default:
                Log.e(TAG, "Widget is not recognized");
                break;
        }
    }

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

//    private void changeSurfaceViewVisibility() {
//        if(isRecordLayoutVisible) {
//            setSurfaceViewVisibility(View.VISIBLE);
//            //setDrawFlag(true);
//        } else {
//            setSurfaceViewVisibility(View.INVISIBLE);
//            //setDrawFlag(false);
//        }
//    }
//    private void setSurfaceViewVisibility(int visibility) {
//        surfaceView.setVisibility(visibility);
//    }
//
//    private void setDrawFlag(boolean value) {
//        surfaceView.drawFlag = Boolean.valueOf(value);
//    }

    private void changePauseButtonSrc() {
        isPause = !isPause;
        if(isPause) {
            pauseButton.setBackgroundResource(R.drawable.play);
        } else {
            pauseButton.setBackgroundResource(R.drawable.pause);
        }
    }
//
//    private void pauseSurfaceView() {
//        if (!isPause) {
//            surfaceView.drawThread.soundCapture = Boolean.valueOf(false);
//            surfaceView.drawThread.segmentIndex = -1;
//        }
//        else {
//            surfaceView.drawThread.soundCapture = Boolean.valueOf(true);
//
//        }
//    }

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

    }

}
