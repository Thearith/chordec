package com.example.chordec.chordec.SoundSampler;

import android.app.Activity;
import android.media.AudioRecord;
import android.util.Log;

import com.example.chordec.chordec.MainActivity;


public class SoundSampler {

    private static final int  FS = 16000;     // sampling frequency
    public  AudioRecord       audioRecord;
    private int               audioEncoding = 2;
    private int               nChannels = 16;
    private Thread            recordingThread;

    private MainActivity          mActivity;

    public SoundSampler(MainActivity mAct) throws Exception
    {
        mActivity = mAct;

        try {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
            }
            audioRecord = new AudioRecord(1, FS, nChannels, audioEncoding, AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding));
        }
        catch (Exception e) {
            Log.d("SoundActivity mAct", e.getMessage());
            throw new Exception();
        }

        return;

    }


    public void init() throws Exception
    {
        try {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
            }
            audioRecord = new AudioRecord(1, FS, nChannels, audioEncoding, AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding));
        }
        catch (Exception e) {
            Log.d("Error in Init() ", e.getMessage());
            throw new Exception();
        }

        mActivity.bufferSize = AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding);
        mActivity.buffer = new short[mActivity.bufferSize];

        audioRecord.startRecording();

        recordingThread = new Thread()
        {
            public void run()
            {
                while (true)
                {

                    audioRecord.read(mActivity.buffer, 0, mActivity.bufferSize);
                    //mActivity.surfaceView.drawThread.setBuffer(mActivity.buffer);

                }
            }
        };
        recordingThread.start();

        return;

    }

    public void stop() {
        if(audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
    }


}


