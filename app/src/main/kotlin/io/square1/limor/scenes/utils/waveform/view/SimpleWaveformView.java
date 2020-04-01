package io.square1.limor.scenes.utils.waveform.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


import io.square1.limor.R;
import io.square1.limor.scenes.utils.waveform.soundfile.SoundFile;

public class SimpleWaveformView extends View {

    private int mWaveformColor;

    private Paint mWaveformPaint;

    private SoundFile mAudioFile;
    private int mSampleRate;
    private int mSamplesPerFrame;
    private int mNumFrames;
    private double[] mValuesByZoomLevel;
    private boolean mInitialized;
    private int[] mHeightsAtThisZoomLevel;
    private Paint mPlaybackIndicatorPaint;
    private int mPlaybackIndicatorPosition;

    private boolean shouldShowPlayBar;

    public SimpleWaveformView(Context context) {
        this(context, null);
    }

    public SimpleWaveformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public SimpleWaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(false);
        mValuesByZoomLevel = null;
        mHeightsAtThisZoomLevel = null;

    }

    public void setAudioFile(SoundFile audioFile, int color, boolean shouldShowPlayBar) {
        this.shouldShowPlayBar = shouldShowPlayBar;
        mWaveformColor = color;
        mWaveformPaint = new Paint();
        mWaveformPaint.setAntiAlias(false);
        mWaveformPaint.setColor(mWaveformColor);
        mAudioFile = audioFile;
        mSampleRate = mAudioFile.getSampleRate();
        mSamplesPerFrame = mAudioFile.getSamplesPerFrame();
        mNumFrames = mAudioFile.getNumFrames();

        if (shouldShowPlayBar) {
            mPlaybackIndicatorPaint = new Paint();
            mPlaybackIndicatorPaint.setAntiAlias(false);
            mPlaybackIndicatorPaint.setColor(getResources().getColor(R.color.white));
            mPlaybackIndicatorPaint.setStrokeWidth(2);
        }

        computeDoublesForAllZoomLevels();
        mHeightsAtThisZoomLevel = null;
    }

    private void computeDoublesForAllZoomLevels() {
        int numFrames = mAudioFile.getNumFrames();
        int[] frameGains = mAudioFile.getFrameGains();
        double[] smoothedGains = new double[numFrames];
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0];
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0];
            smoothedGains[1] = frameGains[1];
        } else if (numFrames > 2) {
            smoothedGains[0] = (double) (
                    (frameGains[0] / 2.0) +
                            (frameGains[1] / 2.0));
            for (int i = 1; i < numFrames - 1; i++) {
                smoothedGains[i] = (double) (
                        (frameGains[i - 1] / 3.0) +
                                (frameGains[i] / 3.0) +
                                (frameGains[i + 1] / 3.0));
            }
            smoothedGains[numFrames - 1] = (double) (
                    (frameGains[numFrames - 2] / 2.0) +
                            (frameGains[numFrames - 1] / 2.0));
        }

        // Make sure the range is no more than 0 - 255
        double maxGain = 1.0;
        for (int i = 0; i < numFrames; i++) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i];
            }
        }
        double scaleFactor = 1.0;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0;
        int gainHist[] = new int[256];
        for (int i = 0; i < numFrames; i++) {
            int smoothedGain = (int) (smoothedGains[i] * scaleFactor);
            if (smoothedGain < 0)
                smoothedGain = 0;
            if (smoothedGain > 255)
                smoothedGain = 255;

            if (smoothedGain > maxGain)
                maxGain = smoothedGain;

            gainHist[smoothedGain]++;
        }

        // Re-calibrate the min to be 5%
        double minGain = 0;
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

        // Compute the heights
        double[] heights = new double[numFrames];
        double range = maxGain - minGain;
        for (int i = 0; i < numFrames; i++) {
            double value = (smoothedGains[i] * scaleFactor - minGain) / range;
            if (value < 0.0)
                value = 0.0;
            if (value > 1.0)
                value = 1.0;
            heights[i] = value * value;
        }

        // Level 1 is normal
        mValuesByZoomLevel = new double[numFrames];
        for (int i = 0; i < numFrames; i++) {
            mValuesByZoomLevel[i] = heights[i];
        }

        mInitialized = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAudioFile == null) {
            return;
        }

        if (mHeightsAtThisZoomLevel == null) {
            computeIntsForThisZoomLevel();
        }

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int centerVertical = measuredHeight / 2;

        // Draw waveform
        for (int i = 0; i < measuredWidth; i++) {
            drawWaveformLine(canvas, i, centerVertical - mHeightsAtThisZoomLevel[i], centerVertical + 1 + mHeightsAtThisZoomLevel[i], mWaveformPaint);
            if (shouldShowPlayBar && i == mPlaybackIndicatorPosition) {
                canvas.drawLine(i, 0, i, measuredHeight, mPlaybackIndicatorPaint);
            }
        }
    }

    protected void drawWaveformLine(Canvas canvas, int x, int y0, int y1, Paint paint) {
        canvas.drawLine(x, y0, x, y1, paint);
    }

    private void computeIntsForThisZoomLevel() {
        int halfHeight = (getMeasuredHeight() / 2) - 1;
        int measuredWidth = getMeasuredWidth();
        int valuesCount = mValuesByZoomLevel.length;

        float valuesCountPerPx = (float) valuesCount / measuredWidth;
        mHeightsAtThisZoomLevel = new int[measuredWidth];

        for (int i = 0; i < measuredWidth; i++) {
            int index = (int) (valuesCountPerPx * i);
            if (index >= 0 && index < mValuesByZoomLevel.length) {
                mHeightsAtThisZoomLevel[i] = (int) (mValuesByZoomLevel[index] * halfHeight);
            }
        }
    }

    public void setPlaybackPosition(int position) {
        int frame = millisecsToPixels(position);
        mPlaybackIndicatorPosition = (int) (frame / (float) mNumFrames * getMeasuredHeight());
        invalidate();
    }

    public int millisecsToPixels(int msecs) {
        return (int)((msecs * 1.0 * mSampleRate * 1.8) / (1000.0 * mSamplesPerFrame) + 0.5);
    }

}