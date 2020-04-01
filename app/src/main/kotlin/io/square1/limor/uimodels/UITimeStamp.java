package io.square1.limor.uimodels;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class UITimeStamp implements Parcelable {

    @SerializedName("duration")
    private int duration;
    @SerializedName("end_sample")
    private int endSample;
    @SerializedName("start_sample")
    private int startSample;

    transient private Integer color;

    public UITimeStamp(int duration, int endSample, int startSample) {
        this.duration = duration;
        this.endSample = endSample;
        this.startSample = startSample;
    }

    public UITimeStamp() {
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getEndSample() {
        return endSample;
    }

    public void setEndSample(int endSample) {
        this.endSample = endSample;
    }

    public int getStartSample() {
        return startSample;
    }

    public void setStartSample(int startSample) {
        this.startSample = startSample;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.duration);
        dest.writeInt(this.endSample);
        dest.writeInt(this.startSample);
    }

    protected UITimeStamp(Parcel in) {
        this.duration = in.readInt();
        this.endSample = in.readInt();
        this.startSample = in.readInt();
    }

    public static final Creator<UITimeStamp> CREATOR = new Creator<UITimeStamp>() {
        @Override
        public UITimeStamp createFromParcel(Parcel source) {
            return new UITimeStamp(source);
        }

        @Override
        public UITimeStamp[] newArray(int size) {
            return new UITimeStamp[size];
        }
    };
}
