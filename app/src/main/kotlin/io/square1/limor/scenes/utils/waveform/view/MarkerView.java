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
 */

package io.square1.limor.scenes.utils.waveform.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import io.square1.limor.scenes.utils.waveform.MarkerSet;

public class MarkerView extends androidx.appcompat.widget.AppCompatImageView {

    public final static int START_MARKER = 0;
    public final static int MIDDLE_MARKER = 1;
    public final static int END_MARKER = 2;


    public interface MarkerListener {
        void markerTouchStart(MarkerView marker, float pos);
        void markerTouchMove(MarkerView marker, float pos);
        void markerTouchEnd(MarkerView marker);
        void markerFocus(MarkerView marker);
        void markerDraw();
    }

    private MarkerListener listener;
    private MarkerSet markerSet;
    private int type;

    public MarkerView(Context context) {
        super(context);
        setFocusable(true);
        listener = null;
    }

    public MarkerSet getMarkerSet() {
        return markerSet;
    }

    public void setMarkerSet(MarkerSet markerSet) {
        this.markerSet = markerSet;
    }

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        listener = null;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setListener(MarkerListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (markerSet.isEditMarker() && (type == START_MARKER || type == END_MARKER)) {
            return false;
        }
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                requestFocus();
                listener.markerTouchStart(this, event.getRawX());
                break;
            case MotionEvent.ACTION_MOVE:
                listener.markerTouchMove(this, event.getRawX());
                break;
            case MotionEvent.ACTION_UP:
                listener.markerTouchEnd(this);
                break;
        }
        return true;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus && listener != null) {
            //TODO JJ new the app explote here
            listener.markerFocus(this);
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (listener != null) {
            listener.markerDraw();
        }
    }
}