//package com.example.chordec.chordec.SoundSampler;
//
//import android.app.Activity;
//import android.media.AudioRecord;
//import android.os.Environment;
//import android.util.Log;
//
//import com.example.chordec.chordec.MainActivity;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//
//public class SoundSampler {
//
//    // sampling frequency
//    private static final int  FS = 16000;
//    private static final int  BYPES_PER_ELEMENT = 2; // 2 bytes in 16bit format
//
//    // AudioRecord : for audio sampling
//    public  AudioRecord       audioRecord;
//
//    private MainActivity          mActivity;
//    private String filePath;
//
//    // Thread
//    private Thread            recordingThread;
//    private boolean           isRecording;
//
//    private int               audioEncoding = 2;
//    private int               nChannels = 16;
//
//
//    public SoundSampler(MainActivity mAct, String filePath) throws Exception
//    {
//        mActivity = mAct;
//        this.filePath = filePath;
//
//        try {
//            if (audioRecord != null) {
//                audioRecord.stop();
//                audioRecord.release();
//            }
//            audioRecord = new AudioRecord(1,
//                    FS, nChannels, audioEncoding,
//                    AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding));
//        }
//        catch (Exception e) {
//            Log.d("SoundActivity mAct", e.getMessage());
//            throw new Exception();
//        }
//
//        return;
//
//    }
//
//
//    public void init() throws Exception
//    {
//        // initialize audio record
//        try {
//
//            if (audioRecord != null) {
//                audioRecord.stop();
//                audioRecord.release();
//            }
//
//            audioRecord = new AudioRecord(1, FS, nChannels, audioEncoding,
//                    AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding));
//        }
//        catch (Exception e) {
//            Log.d("Error in Init() ", e.getMessage());
//            throw new Exception();
//        }
//
//        // set buffer size
//        mActivity.bufferSize = AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding);
//        mActivity.buffer = new short[mActivity.bufferSize];
//
//        return;
//
//    }
//
//    public void start() {
//        audioRecord.startRecording();
//        isRecording = true;
//
//        recordingThread = new Thread() {
//
//            public void run() {
//                startRecording();
//            }
//        };
//
//        recordingThread.start();
//    }
//
//    public void stop() {
//        isRecording = false;
//    }
//
//    public void dismiss() {
//        if(audioRecord != null) {
//            audioRecord.stop();
//            audioRecord.release();
//
//            isRecording = false;
//
//            try {
//                recordingThread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            recordingThread = null;
//        }
//    }
//
//    private void startRecording() {
//        writeAudioDataToFile();
//    }
//
//    private void writeAudioDataToFile() {
//        //initialize file outputstream
//        FileOutputStream os = null;
//        File file = new File(Environment.getExternalStorageDirectory(), filePath);
//
//
//        try {
//            os = new FileOutputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        while (isRecording) {
//            // gets the voice output from microphone to byte format
//
//            audioRecord.read(mActivity.buffer, 0, mActivity.bufferSize);
//
//            try {
//                // // writes the data to file from buffer
//                // // stores the voice buffer
//                byte bData[] = short2byte(mActivity.buffer);
//                os.write(bData, 0, mActivity.bufferSize * BYPES_PER_ELEMENT);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            os.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//
//    /*
//    * Helper functions
//    * */
//
//    //convert short to byte
//    private byte[] short2byte(short[] sData) {
//
//        int shortArrsize = sData.length;
//        byte[] bytes = new byte[shortArrsize * 2];
//
//        for (int i = 0; i < shortArrsize; i++) {
//
//            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
//            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
//            sData[i] = 0;
//        }
//
//        return bytes;
//
//    }
//
//}
//
//
