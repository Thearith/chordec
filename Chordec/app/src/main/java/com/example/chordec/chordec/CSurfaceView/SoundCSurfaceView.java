package com.example.chordec.chordec.CSurfaceView;

/**
 * Created by thearith on 25/3/15.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SoundCSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Context         drawContext;
    public  DrawThread       drawThread;
    private SurfaceHolder    drawSurfaceHolder;
    private Boolean          threadExists = false;

    public static volatile Boolean drawFlag = false;

    public SoundCSurfaceView(Context ctx, AttributeSet attributeSet) {
        super(ctx, attributeSet);

        drawContext = ctx;

        init();

    }

    private static final Handler handler = new Handler(){

        public void handleMessage(Message paramMessage)
        {
        }
    };

    public void init()
    {

        if (!threadExists) {

            drawSurfaceHolder = getHolder();
            drawSurfaceHolder.addCallback(this);

            drawThread = new DrawThread(drawSurfaceHolder, drawContext, handler);

            drawThread.setName("" +System.currentTimeMillis());
            drawThread.start();
        }

        threadExists = Boolean.valueOf(true);

        drawFlag     = Boolean.valueOf(true);

        return;

    }


    public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2, int paramInt3)
    {
        drawThread.setSurfaceSize(paramInt2, paramInt3);
    }

    public void surfaceCreated(SurfaceHolder paramSurfaceHolder)
    {

        init();

    }

    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder)
    {
        while (true)
        {
            if (!drawFlag)
                return;
            try
            {
                drawFlag = Boolean.valueOf(false);
                drawThread.join();

            }
            catch (InterruptedException localInterruptedException)
            {
            }
        }

    }


    public class DrawThread extends Thread
    {
        private Bitmap soundBackgroundImage;
        private short[]        soundBuffer;
        private int[]          soundSegmented;

        public  Boolean        soundCapture = Boolean.valueOf(false);

        public  int            FFT_Len      = 1024;
        public  int            segmentIndex = -1;

        private int            soundCanvasHeight = 0;
        private int			   soundCanvasWidth  = 0;

        private Paint		   soundLinePaint;
        private SurfaceHolder  soundSurfaceHolder;
        private int            drawScale   = 8;

        public DrawThread(SurfaceHolder paramContext, Context paramHandler, Handler arg4)
        {
            soundSurfaceHolder = paramContext;

            soundLinePaint     = new Paint();
            soundLinePaint.setAntiAlias(true);
            soundLinePaint.setARGB(255, 255, 0, 0);
            soundLinePaint.setStrokeWidth(10);

            soundBuffer        = new short[2048];

            soundBackgroundImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);


            soundSegmented     = new int[FFT_Len];

        }



        public void doDraw(Canvas canvas)
        {

            soundCanvasHeight  = canvas.getHeight();
            soundCanvasWidth   = canvas.getWidth();

            int height         = soundCanvasHeight;
            int width          = soundCanvasWidth;

            Paint paint = new Paint();
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPaint(paint);

            if (!soundCapture) {

                int xStart = 0;

                while (xStart < width -1)  {

                    int yStart = soundBuffer[xStart] / height * drawScale;
                    int yStop  = soundBuffer[xStart+1] / height * drawScale;

                    int xStart1 = xStart + width/5 ;
                    int xStop1 = xStart + 1 + width/5;

                    int yStart1 = yStart + height/2;
                    int yStop1  = yStop  + height/2;

                    canvas.drawLine(xStart1, yStart1, xStop1, yStop1, soundLinePaint);

                    xStart++;

                }


            } else {

                if (segmentIndex < 0) {
                    segmentIndex = 0;
                    while (segmentIndex < FFT_Len) {
                        soundSegmented[segmentIndex] = soundBuffer[segmentIndex];
                        segmentIndex++;
                    }
                }

                // display the signal in temporal domain
                int xStart = 0;

                while (xStart < width -1)  {

                    int yStart = soundSegmented[xStart] / height * drawScale;
                    int yStop  = soundSegmented[xStart+1] / height * drawScale;

                    int yStart1 = yStart + height/4;
                    int yStop1  = yStop  + height/4;

                    canvas.drawLine(xStart, yStart1, xStart +1, yStop1, soundLinePaint);

                    xStart++;

                }

            }

        }


        public void setBuffer(short[] paramArrayOfShort)
        {
            synchronized (soundBuffer)
            {
                soundBuffer = paramArrayOfShort;
                return;
            }
        }


        public void setSurfaceSize(int canvasWidth, int canvasHeight)
        {
            synchronized (soundSurfaceHolder)
            {
                soundBackgroundImage = Bitmap.createScaledBitmap(soundBackgroundImage, canvasWidth, canvasHeight, true);
                return;
            }
        }


        public void run()
        {

            while (drawFlag)
            {

                Canvas localCanvas = null;
                try
                {
                    localCanvas = soundSurfaceHolder.lockCanvas(null);
                    synchronized (soundSurfaceHolder)
                    {
                        if (localCanvas != null)
                            doDraw(localCanvas);

                    }
                }
                finally
                {
                    if (localCanvas != null)
                        soundSurfaceHolder.unlockCanvasAndPost(localCanvas);
                }
            }
        }


    }


}
