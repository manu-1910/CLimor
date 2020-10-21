package com.limor.app.scenes.utils.statemanager;

import com.limor.app.uimodels.UITimeStamp;

import java.util.ArrayList;

public class Step{

    private long timestamp;
    private String filePath;
    private ArrayList<UITimeStamp> timeStamps;

    public Step() {
    }

    public Step(long timestamp, String filePath, ArrayList<UITimeStamp> timeStamps) {
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

    public ArrayList<UITimeStamp> getTimeStamps() {
        return timeStamps;
    }

    public void setTimeStamps(ArrayList<UITimeStamp> timeStamps) {
        this.timeStamps = timeStamps;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeLong(this.timestamp);
//        dest.writeString(this.filePath);
//        dest.writeTypedList(this.timeStamps);
//    }
//
//    protected Step(Parcel in) {
//        this.timestamp = in.readLong();
//        this.filePath = in.readString();
//        this.timeStamps = in.createTypedArrayList(UITimeStamp.CREATOR);
//    }
//
//    public static final Creator<Step> CREATOR = new Creator<Step>() {
//        @Override
//        public Step createFromParcel(Parcel source) {
//            return new Step(source);
//        }
//
//        @Override
//        public Step[] newArray(int size) {
//            return new Step[size];
//        }
//    };
}