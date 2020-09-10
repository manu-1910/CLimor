package io.square1.limor.scenes.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import io.square1.limor.R;

public class VisualizerView extends View {
    private static final int LINE_WIDTH = 10; // width of visualizer lines
    private static final int LINE_SCALE = 65; // scales visualizer lines
    private List<Float> amplitudes; // amplitudes for line lengths
    private int width; // width of this View
    private int height; // height of this View
    private Paint linePaint; // specifies line drawing characteristics


    // constructor
    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        linePaint = new Paint(); // create Paint for lines
        linePaint.setColor(getResources().getColor(R.color.brandPrimary500)); // set color to green
        linePaint.setStrokeWidth(LINE_WIDTH); // set stroke width
        linePaint.setDither(true);                    // set the dither to true
        linePaint.setStyle(Paint.Style.STROKE);       // set to STOKE
        linePaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        linePaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        linePaint.setPathEffect(new CornerPathEffect(10) );   // set the path effect when they join.
        linePaint.setAntiAlias(true);
    }


    // called when the dimensions of the View change
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //We paint 1 bar and set the margin of other bar, so, our width of the canvas will be width/2 because we need the double of space
        width = w / 2; // new width of this View
        height = h; // new height of this View

        // we will calculate the new capacity because from now on, there won't fit the same ammount of items than before
        int newCapacity = width / LINE_WIDTH;
        // we create an auxList that will help us handle what to do with the previous items
        List<Float> newList = new ArrayList<>(newCapacity);

        // if now view is larger and therefore, now we can fit more items, we'll put all the items
        // in the aux list
        if(amplitudes != null && newCapacity > amplitudes.size()) {
            newList.addAll(amplitudes);

            // if previous list is not null and has more items than the amount we can fit now
            // then we'll have to truncate the previous list
        } else if(amplitudes != null) {
            int difference = amplitudes.size() - newCapacity;
            newList.addAll(amplitudes.subList(difference, amplitudes.size() - 1));
        }

        // and now let's assign that aux list to our original list
        amplitudes = newList;
    }


    // clear all amplitudes to prepare for a new visualization
    public void clear() {
        amplitudes.clear();
    }


    // add the given amplitude to the amplitudes ArrayList
    public void addAmplitude(float amplitude) {
        amplitudes.add(amplitude); // add newest to the amplitudes ArrayList
        // if the power lines completely fill the VisualizerView
        if (amplitudes.size() * LINE_WIDTH >= width) {
            amplitudes.remove(0); // remove oldest power value
        }
    }


    // draw the visualizer with scaled lines representing the amplitudes
    @Override
    public void onDraw(Canvas canvas) {
        int middle = height / 2; // get the vertical middle of the View
        float curX = 0; // start curX at zero

        // for each item in the amplitudes ArrayList
        for (float power : amplitudes) {

            if (power == 0f){ //Will paint 1 point of 1.0 height in case that we receive a 0 amplitude
                power = 1f;
            }
            float scaledHeight = power / LINE_SCALE; // scale the power
            curX = curX + LINE_WIDTH; // increase X by LINE_WIDTH

            // draw a line representing this item in the amplitudes ArrayList
            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle - scaledHeight / 2, linePaint);

            //Increment X exactly the LINE_WIDTH width margin
            curX = curX + LINE_WIDTH;
        }
    }


}
