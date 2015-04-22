package com.example.chordec.chordec.TarsosDSP;

/*
*      _______                       _____   _____ _____
*     |__   __|                     |  __ \ / ____|  __ \
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|
*
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
*
*/


import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioInputStream;

/**
 * The Factory creates {@link be.tarsos.dsp.AudioDispatcher} objects from the
 * configured default microphone of an Android device.
 * It depends on the android runtime and does not work on the standard Java runtime.
 *
 * @author Joren Six
 * @see be.tarsos.dsp.AudioDispatcher
 */
public class AudioDispatcherFactory {
    private static  AudioRecord audioInputStream = null;
    private static Thread recordingThread = null;
    private static boolean isRecording = false;
    private static int minAudioBufferSize = 0;
    private static short[] buffer;
    private static final String AUDIO_RECORDER_FOLDER = "Chordec";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.pcm";

    private static String fileChordPath = "";
    private static boolean isSetFilePath = false;
    /**
     * Create a new AudioDispatcher connected to the default microphone.
     *
     * @param sampleRate
     *            The requested sample rate.
     * @param audioBufferSize
     *            The size of the audio buffer (in samples).
     *
     * @param bufferOverlap
     *            The size of the overlap (in samples).
     * @return A new AudioDispatcher
     */
    public static AudioDispatcher fromDefaultMicrophone(final int sampleRate, final int audioBufferSize, final int bufferOverlap) {
        minAudioBufferSize = AudioRecord.getMinBufferSize(sampleRate, android.media.AudioFormat.CHANNEL_IN_MONO, android.media.AudioFormat.ENCODING_PCM_16BIT);
        int minAudioBufferSizeInSamples =  minAudioBufferSize/2;
        if(minAudioBufferSizeInSamples <= audioBufferSize ){
            audioInputStream = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, android.media.AudioFormat.CHANNEL_IN_MONO, android.media.AudioFormat.ENCODING_PCM_16BIT, audioBufferSize * 2);
            TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(sampleRate, 16,1, true, false);
            TarsosDSPAudioInputStream audioStream = new AndroidAudioInputStream(audioInputStream, format);
            //start recording ! Opens the stream.
            //audioInputStream.startRecording();

            return new AudioDispatcher(audioStream,audioBufferSize,bufferOverlap);
        }else{
            new IllegalArgumentException("Buffer size too small should be at least " + (minAudioBufferSize *2));
            return null;
        }
    }
    public static void startRecording() {
        audioInputStream.startRecording();
        isRecording = true;
        buffer = new short[4088];
//        recordingThread = new Thread(new Runnable() {
//
//            public void run() {
//
//                if(!isSetFilePath) {
//                    setTempFilename();
//                    isSetFilePath = true;
//                }
//
//                writeAudioDataToFile();
//
//            }
//        }, "AudioRecorder Thread");
//        recordingThread.start();
    }


    private static void writeAudioDataToFile() {

        // Write the output audio in byte
        byte data[] = new byte[minAudioBufferSize];

        String filename = getFilename();
        System.out.println(" file name name is " + filename);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;
        while (isRecording) {
            audioInputStream.read(buffer, 0, buffer.length);

            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                try {
                    // // writes the data to file from buffer
                    // // stores the voice buffer

                    // short[] shorts = new short[bytes.length/2];
                    // to turn bytes to shorts as either big endian or little
                    // endian.
                    // ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

                    // to turn shorts back to bytes.
                    byte[] bytes2 = new byte[buffer.length * 2];
                    ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN)
                            .asShortBuffer().put(buffer);

                    os.write(bytes2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setTempFilename() {
        // Creates the temp file to store buffer
        System.out.println("---4-1--");
        String filepath = Environment.getExternalStorageDirectory().getPath();
        System.out.println("file path is " + filepath);
        System.out.println("---4-2--");
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);
        System.out.println("---4-3--");

        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);
        System.out.println("---4-4--");

        if (tempFile.exists())
            tempFile.delete();
        System.out.println("---4-5--");

        fileChordPath = (file.getAbsolutePath() + "/" + (int)(Math.random()*Integer.MAX_VALUE) + "-" +
                AUDIO_RECORDER_TEMP_FILE);
    }

    public static String getFilename() {
        return fileChordPath;
    }

    public static void stopRecording() {
        // stops the recording activity

        if (null != audioInputStream) {
            isRecording = false;

            audioInputStream.stop();
            audioInputStream.release();

            audioInputStream = null;
            recordingThread = null;

            isSetFilePath = false;
        }

        // copy the recorded file to original copy & delete the recorded copy
        //copyWaveFile(getTempFilename(), getFilename());
        //deleteTempFile();
    }


}