package io.square1.limor.scenes.utils.waveform.view;

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified by Anna Stępień
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.square1.limor.R;
import io.square1.limor.scenes.utils.waveform.MarkerSet;
import io.square1.limor.scenes.utils.waveform.WaveformFragment;
import io.square1.limor.scenes.utils.waveform.soundfile.SoundFile;


public class WaveformView extends View {

    public interface WaveformListener {
        void waveformTouchStart(float x);
        void waveformTouchMove(float x);
        void waveformTouchEnd(float x, float y);
        void waveformFling(float x);
        void waveformDraw();
        void waveformZoomIn();
        void waveformZoomOut();
    }

    // region Variables

    protected WaveformListener listener;
    protected SoundFile soundFile;
    protected GestureDetector gestureDetector;
    protected ScaleGestureDetector scaleGestureDetector;

    protected Paint gridPaint;
    protected Paint selectedLinePaint;
    protected Paint unselectedLinePaint;
    protected Paint unselectedBackgroundPaint;
    protected Paint selectedBackgroundPaint;
    protected Paint borderLinePaint;
    protected Paint greyBackgroundPaint;
    protected Paint dividerBackgroundPaint;
    protected Paint playbackLinePaint;
    protected Paint timeCodePaint;
    protected Paint timeCodePaintBlack;
    protected Paint separatorLine; //TODO JJ
    protected Paint testLine; //TODO JJ

    protected int[] lenByZoomLevel;
    protected float[] zoomFactorByZoomLevel;
    protected int zoomLevel;
    protected int numZoomLevels;
    protected int sampleRate;
    protected int samplesPerFrame;
    protected int offset;
    protected int playbackPos;
    protected float density;
    protected float initialScaleSpanX;
    protected float initialScaleSpanY;
    protected boolean initialized;
    protected float range;
    protected float scaleFactor;
    protected float minGain;
    protected ArrayList<MarkerSet> markerSets;
    protected int topOffset;

    public static final long ONE_SECOND = 1000;
    public static final long ONE_MINUTE = ONE_SECOND * 60;
    public static final long ONE_HOUR = ONE_MINUTE * 60;

    public static final int TEXT_SIZE_13 = 13;
    public static final int TEXT_SIZE_8 = 8;

    public static int LINE_WIDTH = 10;


    int lastStart = 0;
    int laMedia = 0;
    // endregion

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setFocusable(false);

        gridPaint = new Paint();
        gridPaint.setAntiAlias(false);
        gridPaint.setColor(getResources().getColor(R.color.brandSecondary500)); //grid_line

        selectedLinePaint = new Paint();
        //selectedLinePaint.setAntiAlias(false);
        //selectedLinePaint.setColor(getResources().getColor(R.color.brandPrimary500));

        selectedLinePaint.setColor(getResources().getColor(R.color.white));
        //selectedLinePaint.setStrokeWidth(dpToPx(context, LINE_WIDTH)); // set stroke width
        selectedLinePaint.setStrokeWidth(LINE_WIDTH); // set stroke width
        selectedLinePaint.setDither(true);                    // set the dither to true
        selectedLinePaint.setStyle(Paint.Style.STROKE);       // set to STOKE
        selectedLinePaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        selectedLinePaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        selectedLinePaint.setPathEffect(new CornerPathEffect(10) );   // set the path effect when they join.
        //selectedLinePaint.setAntiAlias(true);




        testLine = new Paint();
        testLine.setAntiAlias(false);
        testLine.setColor(getResources().getColor(R.color.green500)); //brandSecondary500
        //testLine.setStrokeWidth(10); // set stroke width



        unselectedLinePaint = new Paint();
        unselectedLinePaint.setAntiAlias(false);
        unselectedLinePaint.setColor(getResources().getColor(R.color.blue500)); //waveform_unselected

        unselectedBackgroundPaint = new Paint();
        unselectedBackgroundPaint.setAntiAlias(false);
        unselectedBackgroundPaint.setColor(getResources().getColor(R.color.waveform_unselected_bkgnd_overlay)); // brandSecondary500 //Este está OK

        greyBackgroundPaint = new Paint();
        greyBackgroundPaint.setAntiAlias(false);
        greyBackgroundPaint.setColor(getResources().getColor(R.color.brandSecondary500)); //green500

        dividerBackgroundPaint = new Paint();
        dividerBackgroundPaint.setAntiAlias(false);
        dividerBackgroundPaint.setColor(getResources().getColor(R.color.brandSecondary600)); //brandSecondary700

        selectedBackgroundPaint = new Paint();

        borderLinePaint = new Paint();
        borderLinePaint.setAntiAlias(true);
        borderLinePaint.setStrokeWidth(1.5f);
        borderLinePaint.setPathEffect(new DashPathEffect(new float[]{3.0f, 2.0f}, 0.0f));
        borderLinePaint.setColor(getResources().getColor(R.color.selection_border));

        playbackLinePaint = new Paint();
        playbackLinePaint.setAntiAlias(false);
        playbackLinePaint.setColor(getResources().getColor(R.color.brandPrimary500));

        //TODO JJ
        separatorLine = new Paint();
        separatorLine.setAntiAlias(false);
        separatorLine.setColor(getResources().getColor(R.color.brandSecondary100));

        timeCodePaint = new Paint();
        timeCodePaint.setTextSize(TEXT_SIZE_13);
        timeCodePaint.setAntiAlias(true);
        timeCodePaint.setColor(getResources().getColor(R.color.brandSecondary100)); //Éste es el color de texto de la barra de tiempos

        timeCodePaintBlack = new Paint();
        timeCodePaintBlack.setTextSize(TEXT_SIZE_13);
        timeCodePaintBlack.setAntiAlias(true);
        timeCodePaintBlack.setColor(getResources().getColor(R.color.marker_time_text)); //Éste es el color del texto del tiempo cuando creas un Marker


        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        listener.waveformFling(vx);
                        return true;
                    }
                });
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    public boolean onScaleBegin(ScaleGestureDetector d) {
                        initialScaleSpanX = Math.abs(d.getCurrentSpanX());
                        initialScaleSpanY = Math.abs(d.getCurrentSpanY());
                        return true;
                    }
                    public boolean onScale(ScaleGestureDetector d) {
                        float scaleX = Math.abs(d.getCurrentSpanX());
                        float scaleY = Math.abs(d.getCurrentSpanY());
                        if (scaleX - initialScaleSpanX > 20 || scaleY - initialScaleSpanY > 20) {
                            listener.waveformZoomIn();
                            initialScaleSpanX = scaleX;
                            initialScaleSpanY = scaleY;
                        }
                        if (scaleX - initialScaleSpanX < -20 || scaleY - initialScaleSpanY < -20) {
                            listener.waveformZoomOut();
                            initialScaleSpanX = scaleX;
                            initialScaleSpanY = scaleY;
                        }
                        return true;
                    }
                });

        soundFile = null;
        lenByZoomLevel = null;
        offset = 0; //TODO JJ había un 0
        playbackPos = -1;
        density = 1.0f;
        initialized = false;
        markerSets = new ArrayList<>();
        topOffset = dpToPx(context, 26);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                listener.waveformTouchStart(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getY() > dpToPx(getContext(), 72) && event.getY() < (getMeasuredHeight() - dpToPx(getContext(), 40))) {
                    listener.waveformTouchMove(event.getX());
                }
                break;
            case MotionEvent.ACTION_UP:
                listener.waveformTouchEnd(event.getX(), event.getY());
                break;
        }
        return true;
    }

    public boolean hasSoundFile() {
        return soundFile != null;
    }

    public void setSoundFile(SoundFile soundFile) {
        this.soundFile = soundFile;
        sampleRate = this.soundFile.getSampleRate();
        samplesPerFrame = this.soundFile.getSamplesPerFrame();
        computeDoublesForAllZoomLevels();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public boolean canZoomIn() {
        return (zoomLevel < numZoomLevels - 1);
    }

    public void zoomIn() {
        if (canZoomIn()) {
            zoomLevel++;
            float factor = lenByZoomLevel[zoomLevel] / (float) lenByZoomLevel[zoomLevel - 1];
            for (MarkerSet markerSet : markerSets) {
                int mSelectionStart = (int)(markerSet.getStartPos() * factor);
                markerSet.setStartPos(mSelectionStart);
                int mSelectionMiddle = (int)(markerSet.getMiddlePos() * factor);
                markerSet.setMiddlePos(mSelectionMiddle);
                int mSelectionEnd = (int)(markerSet.getEndPos() * factor);
                markerSet.setEndPos(mSelectionEnd);
            }
            int offsetCenter = offset + (int) (getMeasuredWidth() / factor);
            offsetCenter *= factor;
            offset = offsetCenter - (int) (getMeasuredWidth() / factor);
            if (offset < 0) {
                offset = 0;
            }
            invalidate();
        }
    }

    public boolean canZoomOut() {
        return (zoomLevel > 0);
    }

    public void zoomOut() {
        if (canZoomOut()) {
            zoomLevel--;
            float factor = lenByZoomLevel[zoomLevel + 1] / (float) lenByZoomLevel[zoomLevel];
            for (MarkerSet markerSet : markerSets) {
                int mSelectionStart = (int)(markerSet.getStartPos() / factor);
                markerSet.setStartPos(mSelectionStart);
                int mSelectionMiddle = (int)(markerSet.getMiddlePos() / factor);
                markerSet.setMiddlePos(mSelectionMiddle);
                int mSelectionEnd = (int)(markerSet.getEndPos() / factor);
                markerSet.setEndPos(mSelectionEnd);
            }
            int offsetCenter = (int) (offset + getMeasuredWidth() / factor);
            offsetCenter /= factor;
            offset = offsetCenter - (int) (getMeasuredWidth() / factor);
            if (offset < 0) {
                offset = 0;
            }
            invalidate();
        }
    }

    public int maxPos() {
        return lenByZoomLevel[zoomLevel];
    }

    public int secondsToFrames(double seconds) {
        return (int) (1.0 * seconds * sampleRate / samplesPerFrame + 0.5);
    }

    public int secondsToPixels(double seconds) {
        double z = zoomFactorByZoomLevel[zoomLevel]; //TODO JJ original line
        //double z = 1.0f;
        return (int) (z * seconds * sampleRate / samplesPerFrame + 0.5);
    }

    public double pixelsToSeconds(int pixels) {
        double z = zoomFactorByZoomLevel[zoomLevel]; //TODO JJ original line
        //double z = 1.0f;
        return (pixels * (double) samplesPerFrame / (sampleRate * z));
    }

    public int millisecsToPixels(int msecs) {
        double z = zoomFactorByZoomLevel[zoomLevel]; //TODO JJ original line
        //double z = 1.0f;
        return (int) ((msecs * 1.0 * sampleRate * z) / (1000.0 * samplesPerFrame) + 0.5);
    }

    public int pixelsToMillisecs(int pixels) {
        if (zoomFactorByZoomLevel == null) {
            return -1;
        }
        double z = zoomFactorByZoomLevel[zoomLevel]; //TODO JJ original line
        //double z = 1.0f;
        //return (int) (pixels * (1000.0 * samplesPerFrame) / (sampleRate * z) + 0.5); //TODO JJ original line
        return (int) (pixels * (1000.0 * samplesPerFrame) / (sampleRate * z) + 0.5);
    }

    public void setParameters(ArrayList<MarkerSet> markerSets, int offset) {
        this.markerSets = markerSets;
        this.offset = offset;
    }

    public int getOffset() {
        return offset;  //TODO JJ original line
        //return offset + LINE_WIDTH;
    }

    public void setPlayback(int pos) {
        playbackPos = pos; //TODO JJ original line
        //playbackPos = pos + LINE_WIDTH;
    }

    public void setListener(WaveformListener listener) {
        this.listener = listener;
    }

    public void recomputeHeights(float density) {
        this.density = density;
        timeCodePaint.setTextSize((int) (TEXT_SIZE_13 * density));
        timeCodePaintBlack.setTextSize((int) (TEXT_SIZE_13 * density));
        invalidate();
    }

    protected void drawWaveformLine(Canvas canvas, int x, int y0, int y1, Paint paint) {
        canvas.drawLine(x, y0, x, y1, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (soundFile == null) {
            return;
        }
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int start = offset;
        int width = lenByZoomLevel[zoomLevel] - start;
        int ctr = measuredHeight / 2 + dpToPx(getContext(), 12);//TODO JJ 12

        if (width > measuredWidth) {
            width = measuredWidth;
        }

        double onePixelInSecs = pixelsToSeconds(1); //TODO JJ aquí había un 1
        double timeCodeIntervalSecs = 1.0;
        int factor = 1; //TODO JJ aquí había un 1

        while (timeCodeIntervalSecs / onePixelInSecs < 50) {
            timeCodeIntervalSecs = 5.0 * factor; //TODO JJ 5.0
            factor++;
        }

        int i = 0;
        int countTen = 0;
        int startDifference = 0;

        while (i < width) {

            double h = (getScaledHeight(zoomFactorByZoomLevel[zoomLevel], start + i) * getMeasuredHeight() / 2) * 0.5; // scale the wave here
            int height = (int)h;

            /*if (countTen > 19){
                if(height == 0){
                    height = 1;
                }
                height = laMedia/20;
                laMedia = 0;
                drawWaveformLine(canvas, i, ctr + height, ctr + 1 - height, selectedLinePaint); //selectedLinePaint  //testLine
                countTen = 1;
            }else{
                //drawWaveformLine(canvas, i, ctr + height, ctr + 1 - height, unselectedLinePaint); //greyBackgroundPaint
                laMedia = laMedia + height;
            }*/

            drawWaveformLine(canvas, i, ctr + height, ctr + 1 - height, selectedLinePaint);

            i++;
            //countTen++;
            //lastStart = start;
        }


        //Background color when a marker is created
        int j = 0;
        while (j < width){
            // Waveform background
            //drawWaveformLine(canvas, i, 0, measuredHeight, unselectedBackgroundPaint);  //Background negro del waveform

            //Background of the markers
            for (MarkerSet markerSet : markerSets) {
                if (j + start >= markerSet.getStartPos() && j + start < markerSet.getEndPos()) {
                    selectedBackgroundPaint.setAntiAlias(false);
                    selectedBackgroundPaint.setColor(getResources().getColor(markerSet.getBackgroundColor()));  //TODO JJ Éste es el background cuando seleccionas un marker
                    selectedBackgroundPaint.setAlpha(WaveformFragment.isEditMode && ! markerSet.isEditMarker() ? 60 : 120);
                    drawWaveformLine(canvas, j, topOffset, measuredHeight - topOffset, selectedBackgroundPaint);
                }
            }
            j++;
        }


        // Draw borders of the selection and marker timestamps
        for (MarkerSet markerSet : markerSets) {
            // Vertical borders of marker area
            canvas.drawLine(markerSet.getStartPos() - offset + 1f, 0, markerSet.getStartPos() - offset + 1f, measuredHeight - topOffset, borderLinePaint);
            canvas.drawLine(markerSet.getEndPos() - offset + 1f, 0, markerSet.getEndPos() - offset + 1f, measuredHeight - topOffset, borderLinePaint);

            // Right top timestamp
            if (!markerSet.isEditMarker()) {
                float leftEnd = markerSet.getEndPos() + dpToPx(getContext(), 16);
                float topEnd = measuredHeight - topOffset + dpToPx(getContext(), 16);
                String timeCode = "" + getLengthFromEpochForPlayer(pixelsToMillisecs(markerSet.getEndPos()));
                canvas.drawText(timeCode, leftEnd, topEnd, timeCodePaintBlack);
            }

            // Left bottom timestamp
            float leftStart = markerSet.getStartPos() - dpToPx(getContext(), 44);
            float topStart = measuredHeight - topOffset + dpToPx(getContext(), 16);
            String timeCodeStart = "" + getLengthFromEpochForPlayer(pixelsToMillisecs(markerSet.getStartPos()));
            canvas.drawText(timeCodeStart, leftStart, topStart, timeCodePaintBlack);
        }

        // Top background
        canvas.drawRect(0f, 0f, (float)getMeasuredWidth(), topOffset * 2, greyBackgroundPaint);
        canvas.drawRect(0f, topOffset - 3, (float)getMeasuredWidth(), topOffset - 1, dividerBackgroundPaint);

        //Light grey separator lines
        canvas.drawLine(0f, topOffset, (float)getMeasuredWidth(), topOffset, separatorLine);
        canvas.drawLine(0f, topOffset * 2, (float)getMeasuredWidth(), topOffset * 2, separatorLine);
        canvas.drawLine(0f, measuredHeight - topOffset - 1f, (float)getMeasuredWidth(), measuredHeight - topOffset - 1f, separatorLine);

        // Bottom background
        //canvas.drawRect(0f, measuredHeight - topOffset / 2, (float)getMeasuredWidth(), measuredHeight, greyBackgroundPaint); //greyBackgroundPaint
        canvas.drawRect(0f, measuredHeight, (float)getMeasuredWidth(), measuredHeight, playbackLinePaint); //greyBackgroundPaint

        // Grid
        float eight = (getMeasuredWidth() / 4f) / 2f;
        // Draw text
        for (int count = 1; count < 8; count++) {
            String timeCode = "" + getLengthFromEpochForPlayer(pixelsToMillisecs((int)(eight * count) + offset));
            float offsetText = (float) (0.5 * timeCodePaint.measureText(timeCode));
            canvas.drawText(timeCode, eight * count - offsetText , (int) (16 * density), timeCodePaint);
        }

        int xPlayPos = 0;
        while (xPlayPos < width) {
            // Playback marker //TODO JJ linea vertical cuando le das al play
            if (xPlayPos + start == playbackPos) {
                canvas.drawRect(xPlayPos, topOffset * 2 , xPlayPos + dpToPx(getContext(), 2), measuredHeight - topOffset, playbackLinePaint);
            }
            xPlayPos++;
        }

        if (listener != null) {
            listener.waveformDraw();
        }
    }



    protected float getGain(int i, int numFrames, int[] frameGains) {
        int x = Math.min(i, numFrames - 1);
        if (numFrames < 2) {
            return frameGains[x];
        } else {
            if (x == 0) {
                return (frameGains[0] / 2.0f) + (frameGains[1] / 2.0f);
            } else if (x == numFrames - 1) {
                return (frameGains[numFrames - 2] / 2.0f) + (frameGains[numFrames - 1] / 2.0f);
            } else {
                return (frameGains[x - 1] / 3.0f) + (frameGains[x] / 3.0f) + (frameGains[x + 1] / 3.0f);
            }
        }
    }


    protected float getHeight(int i, int numFrames, int[] frameGains, float scaleFactor, float minGain, float range) {
        float value = (getGain(i, numFrames, frameGains) * scaleFactor - minGain) / range;
        if (value < 0.0) {
            value = 0.0f;
        }
        if (value > 1.0) {
            value = 1.0f;
        }
        return value;
    }

    // Called once when a new sound file is added
    protected void computeDoublesForAllZoomLevels() {
        int numFrames = soundFile.getNumFrames();
        // Make sure the range is no more than 0 - 255
        //float maxGain = 1.0f; //TODO JJ original line
        float maxGain = 1.0f;
        for (int i = 0; i < numFrames; i++) {
            float gain = getGain(i, numFrames, soundFile.getFrameGains());
            if (gain > maxGain) {
                maxGain = gain;
            }
        }
        //scaleFactor = 1.0f; //TODO JJ original line
        scaleFactor = 1.0f;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }
        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0;
        int gainHist[] = new int[256];
        for (int i = 0; i < numFrames; i++) {
            int smoothedGain = (int) (getGain(i, numFrames, soundFile.getFrameGains()) * scaleFactor);
            if (smoothedGain < 0) {
                smoothedGain = 0;
            }
            if (smoothedGain > 255) {
                smoothedGain = 255;
            }
            if (smoothedGain > maxGain) {
                maxGain = smoothedGain;
            }
            gainHist[smoothedGain]++;
        }

        // Re-calibrate the min to be 5%
        minGain = 0;
        int sum = 0;
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[(int) minGain];
            minGain++;
        }

        // Re-calibrate the max to be 99%
        sum = 0;

        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[(int) maxGain];
            maxGain--;
        }

        range = maxGain - minGain;
        numZoomLevels = 5;
        lenByZoomLevel = new int[5];
        zoomFactorByZoomLevel = new float[5];

        float ratio = getMeasuredWidth() / (float) numFrames;

        if (ratio < 1) {
            lenByZoomLevel[0] = Math.round(numFrames * ratio);
            zoomFactorByZoomLevel[0] = ratio;
            lenByZoomLevel[1] = numFrames;
            zoomFactorByZoomLevel[1] = 1.0f;
            lenByZoomLevel[2] = numFrames * 2;
            zoomFactorByZoomLevel[2] = 2.0f;
            lenByZoomLevel[3] = numFrames * 3;
            zoomFactorByZoomLevel[3] = 3.0f;
            lenByZoomLevel[4] = numFrames * 4;
            zoomFactorByZoomLevel[4] = 4.0f;
            zoomLevel = 0;
        } else {
            lenByZoomLevel[0] = numFrames;
            zoomFactorByZoomLevel[0] = 1.0f;
            lenByZoomLevel[1] = numFrames * 2;
            zoomFactorByZoomLevel[1] = 2f;
            lenByZoomLevel[2] = numFrames * 3;
            zoomFactorByZoomLevel[2] = 3.0f;
            lenByZoomLevel[3] = numFrames * 4;
            zoomFactorByZoomLevel[3] = 4.0f;
            lenByZoomLevel[4] = numFrames * 5;
            zoomFactorByZoomLevel[4] = 5.0f;
            zoomLevel = 0;
            for (int i = 0; i < 5; i++) {
                if (lenByZoomLevel[zoomLevel] - getMeasuredWidth() > 0) {
                    break;
                } else {
                    zoomLevel = i;
                }
            }
        }
        initialized = true;
    }


    protected float getZoomedInHeight(float zoomLevel, int i) {
        int f = (int) zoomLevel;

        //System.out.println(String.format("%20s %20s %20s %20s %20s", "Sound.NºFrames", "Sound.FramesGain", "scaleFactor", "minGain", "range"));
        //System.out.println(String.format("%20s %20s %20s %20s %20s", soundFile.getNumFrames(), soundFile.getFrameGains(), scaleFactor, minGain, range));

        if (i == 0) {
            return 0.5f * getHeight(0, soundFile.getNumFrames(), soundFile.getFrameGains(), scaleFactor, minGain, range);
        }
        if (i == 1) {
            return getHeight(0, soundFile.getNumFrames(), soundFile.getFrameGains(), scaleFactor, minGain, range);
        }
        if (i % f == 0) {
            float x1 = getHeight(i / f - 1, soundFile.getNumFrames(), soundFile.getFrameGains(), scaleFactor, minGain, range);
            float x2 = getHeight(i / f, soundFile.getNumFrames(), soundFile.getFrameGains(), scaleFactor, minGain, range);
            return 0.5f * (x1 + x2);
        } else if ((i - 1) % f == 0) {
            return getHeight((i - 1) / f, soundFile.getNumFrames(), soundFile.getFrameGains(), scaleFactor, minGain, range);
        }
        return 0;
    }


    protected float getZoomedOutHeight(float zoomLevel, int i) {
        int f = (int) (i / zoomLevel);
        float x1 = getHeight(f, soundFile.getNumFrames(), soundFile.getFrameGains(), scaleFactor, minGain, range);
        float x2 = getHeight(f + 1, soundFile.getNumFrames(), soundFile.getFrameGains(), scaleFactor, minGain, range);
        return 0.5f * (x1 + x2);
    }


    protected float getNormalHeight(int i) {
        return getHeight(i, soundFile.getNumFrames(), soundFile.getFrameGains(), scaleFactor, minGain, range);
    }


    protected float getScaledHeight(float zoomLevel, int i) {
        //TODO JJ original lines
        //if (zoomLevel == 1.0) {
        //    return getNormalHeight(i);
        //} else if (zoomLevel < 1.0) {
        //    return getZoomedOutHeight(zoomLevel, i);
        //}
        //return getZoomedInHeight(zoomLevel, i);

        float mHeight = 0f;
        //System.out.println("---------------------------------------");
        if (zoomLevel == 1.0) {
            mHeight = getNormalHeight(i);
            //System.out.println("getNormalHeight     is: " + mHeight);
        } else if (zoomLevel < 1.0) {
            mHeight = getZoomedOutHeight(zoomLevel, i);
            //System.out.println("getZoomedOutHeight  is: " + mHeight);
        }else{
            mHeight = getZoomedInHeight(zoomLevel, i);
            //System.out.println("getZoomedInHeight  is: " + mHeight);
        }

        System.out.println("Height returned by getScaledHeight  is: " + mHeight);
        return mHeight;

    }


    //TODO
    public static int dpToPx(Context context, int dps) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dps, context.getResources().getDisplayMetrics()));
    }


    public static String getLengthFromEpochForPlayer(long milliSeconds) {
        DateTime time = new DateTime(milliSeconds);
        final DateTime hour = time.secondOfMinute().roundHalfCeilingCopy();
        Date date = new Date(milliSeconds);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, hour.getSecondOfMinute());
        if (milliSeconds <= ONE_HOUR) {
            return String.format(Locale.getDefault(), "%02d:%02d", calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", calendar.get(Calendar.HOUR) - 1, calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        }
    }


}























































//
//
//
//package io.square1.limor.scenes.utils.waveform.view;
//
///*
// * Copyright (C) 2008 Google Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.CornerPathEffect;
//import android.graphics.DashPathEffect;
//import android.graphics.Paint;
//import android.util.AttributeSet;
//import android.view.GestureDetector;
//import android.view.MotionEvent;
//import android.view.ScaleGestureDetector;
//import android.view.View;
//
//
//import java.util.List;
//import java.util.NavigableMap;
//import java.util.TreeMap;
//
//import io.square1.limor.R;
//import io.square1.limor.scenes.utils.waveform.Segment;
//import io.square1.limor.scenes.utils.waveform.soundfile.SoundFile;
//
//
///**
// * WaveformView is an Android view that displays a visual representation
// * of an audio waveform.  It retrieves the frame gains from a CheapSoundFile
// * object and recomputes the shape contour at several zoom levels.
// * <p/>
// * This class doesn't handle selection or any of the touch interactions
// * directly, so it exposes a listener interface.  The class that embeds
// * this view should add itself as a listener and make the view scroll
// * and respond to other events appropriately.
// * <p/>
// * WaveformView doesn't actually handle selection, but it will just display
// * the selected part of the waveform in a different color.
// * <p/>
// * Modified by Anna Stępień <anna.stepien@semantive.com>
// */
//public class WaveformView extends View {
//
//    public static final String TAG = "WaveformView";
//
//    public interface WaveformListener {
//        void waveformTouchStart(float x);
//
//        void waveformTouchMove(float x);
//
//        void waveformTouchEnd();
//
//        void waveformFling(float x);
//
//        void waveformDraw();
//
//        void waveformZoomIn();
//
//        void waveformZoomOut();
//    }
//
//    // Colors
//    protected Paint mGridPaint;
//    protected Paint mSelectedLinePaint;
//    protected Paint mUnselectedLinePaint;
//    protected Paint mUnselectedBkgndLinePaint;
//    protected Paint mBorderLinePaint;
//    protected Paint mPlaybackLinePaint;
//    protected Paint mTimecodePaint;
//
//    protected SoundFile mSoundFile;
//    protected int[] mLenByZoomLevel;
//    protected float[] mZoomFactorByZoomLevel;
//    protected int mZoomLevel;
//    protected int mNumZoomLevels;
//    protected int mSampleRate;
//    protected int mSamplesPerFrame;
//    protected int mOffset;
//    protected int mSelectionStart;
//    protected int mSelectionEnd;
//    protected int mPlaybackPos;
//    protected float mDensity;
//    protected float mInitialScaleSpan;
//    protected WaveformListener mListener;
//    protected GestureDetector mGestureDetector;
//    protected ScaleGestureDetector mScaleGestureDetector;
//    protected boolean mInitialized;
//
//    protected float range;
//    protected float scaleFactor;
//    protected float minGain;
//
//    protected NavigableMap<Double, Segment> segmentsMap;
//    protected Segment nextSegment;
//
//    public WaveformView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//
//        // We don't want keys, the markers get these
//        setFocusable(false);
//
//        mGridPaint = new Paint();
//        mGridPaint.setAntiAlias(false);
//        mGridPaint.setColor(getResources().getColor(R.color.grid_line));
//
//        mSelectedLinePaint = new Paint();
//        mSelectedLinePaint.setColor(getResources().getColor(R.color.brandPrimary500));
//        mSelectedLinePaint.setStrokeWidth(10); // set stroke width
//        mSelectedLinePaint.setDither(true);                    // set the dither to true
//        mSelectedLinePaint.setStyle(Paint.Style.STROKE);       // set to STOKE
//        mSelectedLinePaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
//        mSelectedLinePaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
//        mSelectedLinePaint.setPathEffect(new CornerPathEffect(10) );   // set the path effect when they join.
//        mSelectedLinePaint.setAntiAlias(true);
//
//        mUnselectedLinePaint = new Paint();
//        mUnselectedLinePaint.setAntiAlias(false);
//        mUnselectedLinePaint.setColor(getResources().getColor(R.color.waveform_unselected));
//        mUnselectedBkgndLinePaint = new Paint();
//        mUnselectedBkgndLinePaint.setAntiAlias(false);
//        mUnselectedBkgndLinePaint.setColor(getResources().getColor(R.color.waveform_unselected_bkgnd_overlay));
//        mBorderLinePaint = new Paint();
//        mBorderLinePaint.setAntiAlias(true);
//        mBorderLinePaint.setStrokeWidth(1.5f);
//        mBorderLinePaint.setPathEffect(new DashPathEffect(new float[]{3.0f, 2.0f}, 0.0f));
//        mBorderLinePaint.setColor(getResources().getColor(R.color.selection_border));
//        mPlaybackLinePaint = new Paint();
//        mPlaybackLinePaint.setAntiAlias(false);
//        mPlaybackLinePaint.setColor(getResources().getColor(R.color.playback_indicator));
//        mTimecodePaint = new Paint();
//        mTimecodePaint.setTextSize(12);
//        mTimecodePaint.setAntiAlias(true);
//        mTimecodePaint.setColor(getResources().getColor(R.color.timecode));
//
//        mGestureDetector = new GestureDetector(
//                context,
//                new GestureDetector.SimpleOnGestureListener() {
//                    public boolean onFling(
//                            MotionEvent e1, MotionEvent e2, float vx, float vy) {
//                        mListener.waveformFling(vx);
//                        return true;
//                    }
//                });
//
//        mScaleGestureDetector = new ScaleGestureDetector(
//                context,
//                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
//                    public boolean onScaleBegin(ScaleGestureDetector d) {
//                        mInitialScaleSpan = Math.abs(d.getCurrentSpanX());
//                        return true;
//                    }
//
//                    public boolean onScale(ScaleGestureDetector d) {
//                        float scale = Math.abs(d.getCurrentSpanX());
//                        if (scale - mInitialScaleSpan > 40) {
//                            mListener.waveformZoomIn();
//                            mInitialScaleSpan = scale;
//                        }
//                        if (scale - mInitialScaleSpan < -40) {
//                            mListener.waveformZoomOut();
//                            mInitialScaleSpan = scale;
//                        }
//                        return true;
//                    }
//                });
//
//        mSoundFile = null;
//        mLenByZoomLevel = null;
//        mOffset = 0;
//        mPlaybackPos = -1;
//        mSelectionStart = 0;
//        mSelectionEnd = 0;
//        mDensity = 1.0f;
//        mInitialized = false;
//        segmentsMap = new TreeMap<>();
//        nextSegment = null;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        mScaleGestureDetector.onTouchEvent(event);
//        if (mGestureDetector.onTouchEvent(event)) {
//            return true;
//        }
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mListener.waveformTouchStart(event.getX());
//                break;
//            case MotionEvent.ACTION_MOVE:
//                mListener.waveformTouchMove(event.getX());
//                break;
//            case MotionEvent.ACTION_UP:
//                mListener.waveformTouchEnd();
//                break;
//        }
//        return true;
//    }
//
//    public boolean hasSoundFile() {
//        return mSoundFile != null;
//    }
//
//    public void setSoundFile(SoundFile soundFile) {
//        mSoundFile = soundFile;
//        mSampleRate = mSoundFile.getSampleRate();
//        mSamplesPerFrame = mSoundFile.getSamplesPerFrame();
//        computeDoublesForAllZoomLevels();
//    }
//
//    public boolean isInitialized() {
//        return mInitialized;
//    }
//
//    public int getZoomLevel() {
//        return mZoomLevel;
//    }
//
//    public void setZoomLevel(int zoomLevel) {
//        mZoomLevel = zoomLevel;
//    }
//
//    public boolean canZoomIn() {
//        return (mZoomLevel < mNumZoomLevels - 1);
//    }
//
//    public void zoomIn() {
//        if (canZoomIn()) {
//            mZoomLevel++;
//            float factor = mLenByZoomLevel[mZoomLevel] / (float) mLenByZoomLevel[mZoomLevel - 1];
//            mSelectionStart *= factor;
//            mSelectionEnd *= factor;
//            int offsetCenter = mOffset + (int) (getMeasuredWidth() / factor);
//            offsetCenter *= factor;
//            mOffset = offsetCenter - (int) (getMeasuredWidth() / factor);
//            if (mOffset < 0)
//                mOffset = 0;
//            invalidate();
//        }
//    }
//
//    public boolean canZoomOut() {
//        return (mZoomLevel > 0);
//    }
//
//    public void zoomOut() {
//        if (canZoomOut()) {
//            mZoomLevel--;
//            float factor = mLenByZoomLevel[mZoomLevel + 1] / (float) mLenByZoomLevel[mZoomLevel];
//            mSelectionStart /= factor;
//            mSelectionEnd /= factor;
//            int offsetCenter = (int) (mOffset + getMeasuredWidth() / factor);
//            offsetCenter /= factor;
//            mOffset = offsetCenter - (int) (getMeasuredWidth() / factor);
//            if (mOffset < 0)
//                mOffset = 0;
//            invalidate();
//        }
//    }
//
//    public int maxPos() {
//        return mLenByZoomLevel[mZoomLevel];
//    }
//
//    public int secondsToFrames(double seconds) {
//        return (int) (1.0 * seconds * mSampleRate / mSamplesPerFrame + 0.5);
//    }
//
//    public int secondsToPixels(double seconds) {
//        double z = mZoomFactorByZoomLevel[mZoomLevel];
//        return (int) (z * seconds * mSampleRate / mSamplesPerFrame + 0.5);
//    }
//
//    public double pixelsToSeconds(int pixels) {
//        double z = mZoomFactorByZoomLevel[mZoomLevel];
//        return (pixels * (double) mSamplesPerFrame / (mSampleRate * z));
//    }
//
//    public int millisecsToPixels(int msecs) {
//        double z = mZoomFactorByZoomLevel[mZoomLevel];
//        return (int) ((msecs * 1.0 * mSampleRate * z) / (1000.0 * mSamplesPerFrame) + 0.5);
//    }
//
//    public int pixelsToMillisecs(int pixels) {
//        double z = mZoomFactorByZoomLevel[mZoomLevel];
//        return (int) (pixels * (1000.0 * mSamplesPerFrame) / (mSampleRate * z) + 0.5);
//    }
//
//    public void setParameters(int start, int end, int offset) {
//        mSelectionStart = start;
//        mSelectionEnd = end;
//        mOffset = offset;
//    }
//
//    public int getStart() {
//        return mSelectionStart;
//    }
//
//    public int getEnd() {
//        return mSelectionEnd;
//    }
//
//    public int getOffset() {
//        return mOffset;
//    }
//
//    public void setPlayback(int pos) {
//        mPlaybackPos = pos;
//    }
//
//    public void setListener(WaveformListener listener) {
//        mListener = listener;
//    }
//
//    public void setSegments(final List<Segment> segments) {
//        if (segments != null) {
//            for (Segment segment : segments) {
//                segmentsMap.put(segment.getStop(), segment);
//            }
//        }
//    }
//
//    public void recomputeHeights(float density) {
//        mDensity = density;
//        mTimecodePaint.setTextSize((int) (12 * density));
//
//        invalidate();
//    }
//
//    protected void drawWaveformLine(Canvas canvas, int x, int y0, int y1, Paint paint) {
//        canvas.drawLine(x, y0, x, y1, paint);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        if (mSoundFile == null)
//            return;
//
//        int measuredWidth = getMeasuredWidth();
//        int measuredHeight = getMeasuredHeight();
//        int start = mOffset;
//        int width = mLenByZoomLevel[mZoomLevel] - start;
//        int ctr = measuredHeight / 2;
//
//        if (width > measuredWidth)
//            width = measuredWidth;
//
//        double onePixelInSecs = pixelsToSeconds(1);
//        boolean onlyEveryFiveSecs = (onePixelInSecs > 1.0 / 50.0);
//        double fractionalSecs = mOffset * onePixelInSecs;
//        int integerSecs = (int) fractionalSecs;
//
//        double timecodeIntervalSecs = 1.0;
//
//        int factor = 1;
//        while (timecodeIntervalSecs / onePixelInSecs < 50) {
//            timecodeIntervalSecs = 5.0 * factor;
//            factor++;
//        }
//
//        int integerTimecode = (int) (fractionalSecs / timecodeIntervalSecs);
//
//        int i = 0;
//        while (i < width) {
//            fractionalSecs += onePixelInSecs;
//            int integerSecsNew = (int) fractionalSecs;
//            if (integerSecsNew != integerSecs) {
//                integerSecs = integerSecsNew;
//                if (!onlyEveryFiveSecs || 0 == (integerSecs % 5)) {
//                    canvas.drawLine(i + 1, 0, i + 1, measuredHeight, mGridPaint);
//                }
//            }
//
//            // Draw waveform
//            drawWaveform(canvas, i, start, measuredHeight, ctr, selectWaveformPaint(i, start, fractionalSecs));
//
//            i++;
//        }
//
//        // If we can see the right edge of the waveform, draw the
//        // non-waveform area to the right as unselected
//        for (i = width; i < measuredWidth; i++) {
//            drawWaveformLine(canvas, i, 0, measuredHeight, mUnselectedBkgndLinePaint);
//        }
//
//        // Draw borders
//        canvas.drawLine(
//                mSelectionStart - mOffset + 0.5f, 30,
//                mSelectionStart - mOffset + 0.5f, measuredHeight,
//                mBorderLinePaint);
//        canvas.drawLine(
//                mSelectionEnd - mOffset + 0.5f, 0,
//                mSelectionEnd - mOffset + 0.5f, measuredHeight - 30,
//                mBorderLinePaint);
//
//        // Draw grid
//        fractionalSecs = mOffset * onePixelInSecs;
//        i = 0;
//        while (i < width) {
//            i++;
//            fractionalSecs += onePixelInSecs;
//            int integerSecs2 = (int) fractionalSecs;
//            int integerTimecodeNew = (int) (fractionalSecs / timecodeIntervalSecs);
//            if (integerTimecodeNew != integerTimecode) {
//                integerTimecode = integerTimecodeNew;
//
//                // Turn, e.g. 67 seconds into "1:07"
//                String timecodeMinutes = "" + (integerSecs2 / 60);
//                String timecodeSeconds = "" + (integerSecs2 % 60);
//                if ((integerSecs2 % 60) < 10) {
//                    timecodeSeconds = "0" + timecodeSeconds;
//                }
//                String timecodeStr = timecodeMinutes + ":" + timecodeSeconds;
//                float offset = (float) (0.5 * mTimecodePaint.measureText(timecodeStr));
//                canvas.drawText(timecodeStr,
//                        i - offset,
//                        (int) (12 * mDensity),
//                        mTimecodePaint);
//            }
//        }
//
//        if (mListener != null) {
//            mListener.waveformDraw();
//        }
//    }
//
//    protected void drawWaveform(final Canvas canvas, final int i, final int start, final int measuredHeight, final int ctr, final Paint paint) {
//        if (i + start < mSelectionStart || i + start >= mSelectionEnd) {
//            drawWaveformLine(canvas, i, 0, measuredHeight,
//                    mUnselectedBkgndLinePaint);
//        }
//
//        int h = (int) (getScaledHeight(mZoomFactorByZoomLevel[mZoomLevel], start + i) * getMeasuredHeight() / 2);
//        drawWaveformLine(
//                canvas, i,
//                ctr - h,
//                ctr + 1 + h,
//                paint);
//
//        if (i + start == mPlaybackPos) {
//            canvas.drawLine(i, 0, i, measuredHeight, mPlaybackLinePaint);
//        }
//    }
//
//    protected Paint selectWaveformPaint(final int i, final int start, final double fractionalSecs) {
//        Paint paint;
//        if (i + start >= mSelectionStart && i + start < mSelectionEnd) {
//            paint = mSelectedLinePaint;
//        } else {
//            //paint = mUnselectedLinePaint;
//            paint = mSelectedLinePaint;
//        }
//
//        if (segmentsMap != null && !segmentsMap.isEmpty()) {
//            if (nextSegment == null) {
//                Double key = segmentsMap.ceilingKey(fractionalSecs);
//                if (key != null) {
//                    nextSegment = segmentsMap.get(segmentsMap.ceilingKey(fractionalSecs));
//                }
//            }
//
//            if (nextSegment != null) {
//                if (nextSegment.getStart().compareTo(fractionalSecs) <= 0 && nextSegment.getStop().compareTo(fractionalSecs) >= 0) {
//                    paint = new Paint();
//                    paint.setAntiAlias(false);
//                    paint.setColor(nextSegment.getColor());
//                    return paint;
//                } else {
//                    Double key = segmentsMap.ceilingKey(fractionalSecs);
//                    if (key != null) {
//                        nextSegment = segmentsMap.get(segmentsMap.ceilingKey(fractionalSecs));
//                    }
//                }
//            }
//        }
//
//        return paint;
//    }
//
//    protected float getGain(int i, int numFrames, int[] frameGains) {
//        int x = Math.min(i, numFrames - 1);
//        if (numFrames < 2) {
//            return frameGains[x];
//        } else {
//            if (x == 0) {
//                return (frameGains[0] / 2.0f) + (frameGains[1] / 2.0f);
//            } else if (x == numFrames - 1) {
//                return (frameGains[numFrames - 2] / 2.0f) + (frameGains[numFrames - 1] / 2.0f);
//            } else {
//                return (frameGains[x - 1] / 3.0f) + (frameGains[x] / 3.0f) + (frameGains[x + 1] / 3.0f);
//            }
//        }
//    }
//
//    protected float getHeight(int i, int numFrames, int[] frameGains, float scaleFactor, float minGain, float range) {
//        float value = (getGain(i, numFrames, frameGains) * scaleFactor - minGain) / range;
//        if (value < 0.0)
//            value = 0.0f;
//        if (value > 1.0)
//            value = 1.0f;
//        return value;
//    }
//
//    /**
//     * Called once when a new sound file is added
//     */
//    protected void computeDoublesForAllZoomLevels() {
//        int numFrames = mSoundFile.getNumFrames();
//
//        // Make sure the range is no more than 0 - 255
//        float maxGain = 1.0f;
//        for (int i = 0; i < numFrames; i++) {
//            float gain = getGain(i, numFrames, mSoundFile.getFrameGains());
//            if (gain > maxGain) {
//                maxGain = gain;
//            }
//        }
//        scaleFactor = 1.0f;
//        if (maxGain > 255.0) {
//            scaleFactor = 255 / maxGain;
//        }
//
//        // Build histogram of 256 bins and figure out the new scaled max
//        maxGain = 0;
//        int gainHist[] = new int[256];
//        for (int i = 0; i < numFrames; i++) {
//            int smoothedGain = (int) (getGain(i, numFrames, mSoundFile.getFrameGains()) * scaleFactor);
//            if (smoothedGain < 0)
//                smoothedGain = 0;
//            if (smoothedGain > 255)
//                smoothedGain = 255;
//
//            if (smoothedGain > maxGain)
//                maxGain = smoothedGain;
//
//            gainHist[smoothedGain]++;
//        }
//
//        // Re-calibrate the min to be 5%
//        minGain = 0;
//        int sum = 0;
//        while (minGain < 255 && sum < numFrames / 20) {
//            sum += gainHist[(int) minGain];
//            minGain++;
//        }
//
//        // Re-calibrate the max to be 99%
//        sum = 0;
//        while (maxGain > 2 && sum < numFrames / 100) {
//            sum += gainHist[(int) maxGain];
//            maxGain--;
//        }
//
//        range = maxGain - minGain;
//
//        mNumZoomLevels = 4;
//        mLenByZoomLevel = new int[4];
//        mZoomFactorByZoomLevel = new float[4];
//
//        float ratio = getMeasuredWidth() / (float) numFrames;
//
//        if (ratio < 1) {
//            mLenByZoomLevel[0] = Math.round(numFrames * ratio);
//            mZoomFactorByZoomLevel[0] = ratio;
//
//            mLenByZoomLevel[1] = numFrames;
//            mZoomFactorByZoomLevel[1] = 1.0f;
//
//            mLenByZoomLevel[2] = numFrames * 2;
//            mZoomFactorByZoomLevel[2] = 2.0f;
//
//            mLenByZoomLevel[3] = numFrames * 3;
//            mZoomFactorByZoomLevel[3] = 3.0f;
//
//            mZoomLevel = 0;
//        } else {
//            mLenByZoomLevel[0] = numFrames;
//            mZoomFactorByZoomLevel[0] = 1.0f;
//
//            mLenByZoomLevel[1] = numFrames * 2;
//            mZoomFactorByZoomLevel[1] = 2f;
//
//            mLenByZoomLevel[2] = numFrames * 3;
//            mZoomFactorByZoomLevel[2] = 3.0f;
//
//            mLenByZoomLevel[3] = numFrames * 4;
//            mZoomFactorByZoomLevel[3] = 4.0f;
//
//            mZoomLevel = 0;
//            for (int i = 0; i < 4; i++) {
//                if (mLenByZoomLevel[mZoomLevel] - getMeasuredWidth() > 0) {
//                    break;
//                } else {
//                    mZoomLevel = i;
//                }
//            }
//        }
//
//        mInitialized = true;
//    }
//
//    protected float getZoomedInHeight(float zoomLevel, int i) {
//        int f = (int) zoomLevel;
//        if (i == 0) {
//            return 0.5f * getHeight(0, mSoundFile.getNumFrames(), mSoundFile.getFrameGains(), scaleFactor, minGain, range);
//        }
//        if (i == 1) {
//            return getHeight(0, mSoundFile.getNumFrames(), mSoundFile.getFrameGains(), scaleFactor, minGain, range);
//        }
//        if (i % f == 0) {
//            float x1 = getHeight(i / f - 1, mSoundFile.getNumFrames(), mSoundFile.getFrameGains(), scaleFactor, minGain, range);
//            float x2 = getHeight(i / f, mSoundFile.getNumFrames(), mSoundFile.getFrameGains(), scaleFactor, minGain, range);
//            return 0.5f * (x1 + x2);
//        } else if ((i - 1) % f == 0) {
//            return getHeight((i - 1) / f, mSoundFile.getNumFrames(), mSoundFile.getFrameGains(), scaleFactor, minGain, range);
//        }
//        return 0;
//    }
//
//    protected float getZoomedOutHeight(float zoomLevel, int i) {
//        int f = (int) (i / zoomLevel);
//        float x1 = getHeight(f, mSoundFile.getNumFrames(), mSoundFile.getFrameGains(), scaleFactor, minGain, range);
//        float x2 = getHeight(f + 1, mSoundFile.getNumFrames(), mSoundFile.getFrameGains(), scaleFactor, minGain, range);
//        return 0.5f * (x1 + x2);
//    }
//
//    protected float getNormalHeight(int i) {
//        return getHeight(i, mSoundFile.getNumFrames(), mSoundFile.getFrameGains(), scaleFactor, minGain, range);
//    }
//
//    protected float getScaledHeight(float zoomLevel, int i) {
//        if (zoomLevel == 1.0) {
//            return getNormalHeight(i);
//        } else if (zoomLevel < 1.0) {
//            return getZoomedOutHeight(zoomLevel, i);
//        }
//        return getZoomedInHeight(zoomLevel, i);
//    }
//}
//
