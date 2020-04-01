package io.square1.limor.scenes.utils.statemanager;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import io.square1.limor.uimodels.UITimeStamp;

public class Step implements Parcelable {

    private long timestamp;
    private String filePath;
    private List<UITimeStamp> timeStamps;

    public Step() {
    }

    public Step(long timestamp, String filePath, List<UITimeStamp> timeStamps) {
        this.timestamp = timestamp;
        this.filePath = filePath;
        this.timeStamps = timeStamps;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<UITimeStamp> getTimeStamps() {
        return timeStamps;
    }

    public void setTimeStamps(List<UITimeStamp> timeStamps) {
        this.timeStamps = timeStamps;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timestamp);
        dest.writeString(this.filePath);
        dest.writeTypedList(this.timeStamps);
    }

    protected Step(Parcel in) {
        this.timestamp = in.readLong();
        this.filePath = in.readString();
        this.timeStamps = in.createTypedArrayList(UITimeStamp.CREATOR);
    }

    public static final Creator<Step> CREATOR = new Creator<Step>() {
        @Override
        public Step createFromParcel(Parcel source) {
            return new Step(source);
        }

        @Override
        public Step[] newArray(int size) {
            return new Step[size];
        }
    };
}