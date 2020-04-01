package io.square1.limor.uimodels;



import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class UIRecordingItem implements Parcelable {


    private long id;
    private String mTitle;
    private String mCaption;
    private String mFilePath;
    private String mEditedFilePath;
    private String mTempPhotoPath;
    private long mLength;
    private long mTime;
    private List<UITimeStamp> timeStamps = new ArrayList<>();


    private Location mLocation;

    public UIRecordingItem() {}

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public String getEditedFilePath() {
        return mEditedFilePath;
    }

    public void setEditedFilePath(String editedFilePath) {
        this.mEditedFilePath = editedFilePath;
    }

    public String getTempPhotoPath() {
        return mTempPhotoPath;
    }

    public void setTempPhotoPath(String mTempPhotoPath) {
        this.mTempPhotoPath = mTempPhotoPath;
    }

    public long getLength() {
        return mLength;
    }

    public void setLength(long length) {
        mLength = length;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public List<UITimeStamp> getTimeStamps() {
        return timeStamps;
    }

    public void setTimeStamps(List<UITimeStamp> timeStamps) {
        this.timeStamps = timeStamps;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.mTitle);
        dest.writeString(this.mCaption);
        dest.writeString(this.mFilePath);
        dest.writeString(this.mEditedFilePath);
        dest.writeString(this.mTempPhotoPath);
        dest.writeLong(this.mLength);
        dest.writeLong(this.mTime);
        dest.writeTypedList(this.timeStamps);
        dest.writeParcelable(this.mLocation, flags);
    }

    protected UIRecordingItem(Parcel in) {
        this.id = in.readLong();
        this.mTitle = in.readString();
        this.mCaption = in.readString();
        this.mFilePath = in.readString();
        this.mEditedFilePath = in.readString();
        this.mTempPhotoPath = in.readString();
        this.mLength = in.readLong();
        this.mTime = in.readLong();
        this.timeStamps = in.createTypedArrayList(UITimeStamp.CREATOR);
        this.mLocation = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Creator<UIRecordingItem> CREATOR = new Creator<UIRecordingItem>() {
        @Override
        public UIRecordingItem createFromParcel(Parcel source) {
            return new UIRecordingItem(source);
        }

        @Override
        public UIRecordingItem[] newArray(int size) {
            return new UIRecordingItem[size];
        }
    };
}
