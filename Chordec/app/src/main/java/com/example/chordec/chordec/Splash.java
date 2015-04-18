package com.example.chordec.chordec;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

import com.easyandroidanimations.library.BounceAnimation;


public class Splash extends Activity {

    private static int SPLASH_TIME_OUT = 2000;
    private static int PULSE_DURATION = 1000;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = (ImageView) findViewById(R.id.logo);

        CountDownTimer timer = new CountDownTimer(SPLASH_TIME_OUT, PULSE_DURATION) {

            @Override
            public void onTick(long l) {
//                new BounceAnimation(logo).setNumOfBounces(1)
//                        .setDuration(PULSE_DURATION).animate();
            }

            @Override
            public void onFinish() {
                Intent i = new Intent(Splash.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        };

        timer.start();
    }

}
