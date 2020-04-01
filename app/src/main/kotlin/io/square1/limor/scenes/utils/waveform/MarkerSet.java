package io.square1.limor.scenes.utils.waveform;

import io.square1.limor.scenes.utils.waveform.view.MarkerView;

public class MarkerSet {

    private MarkerView startMarker, middleMarker, endMarker;
    private long id;
    private int startX, middleX, endX;
    private int startPos, middlePos, endPos, distanceFromTheMiddle;
    private int backgroundColor;
    private boolean startVisible, middleVisible, endVisible, isEditMarker;

    public MarkerView getStartMarker() {
        return startMarker;
    }

    public void setStartMarker(MarkerView startMarker) {
        if (startMarker != null) {
            startMarker.setMarkerSet(this);
        }
        this.startMarker = startMarker;
    }

    public MarkerView getMiddleMarker() {
        return middleMarker;
    }

    public void setMiddleMarker(MarkerView middleMarker) {
        if (middleMarker != null) {
            middleMarker.setMarkerSet(this);
        }
        this.middleMarker = middleMarker;
    }

    public MarkerView getEndMarker() {
        return endMarker;
    }

    public void setEndMarker(MarkerView endMarker) {
        if (endMarker != null) {
            endMarker.setMarkerSet(this);
        }
        this.endMarker = endMarker;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getMiddleX() {
        return middleX;
    }

    public void setMiddleX(int middleX) {
        this.middleX = middleX;
    }

    public int getMiddlePos() {
        return middlePos;
    }

    public void setMiddlePos(int middlePos) {
        this.middlePos = middlePos;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        distanceFromTheMiddle = middlePos - startPos;
        this.startPos = startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        distanceFromTheMiddle = endPos - middlePos;
        this.endPos = endPos;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isStartVisible() {
        return startVisible;
    }

    public void setStartVisible(boolean startVisible) {
        this.startVisible = startVisible;
    }

    public boolean isMiddleVisible() {
        return middleVisible;
    }

    public void setMiddleVisible(boolean middleVisible) {
        this.middleVisible = middleVisible;
    }

    public boolean isEndVisible() {
        return endVisible;
    }

    public void setEndVisible(boolean endVisible) {
        this.endVisible = endVisible;
    }

    public int getDistanceFromTheMiddle() {
        return distanceFromTheMiddle;
    }

    public boolean isEditMarker() {
        return isEditMarker;
    }

    public void setEditMarker(boolean editMarker) {
        isEditMarker = editMarker;
    }

}
