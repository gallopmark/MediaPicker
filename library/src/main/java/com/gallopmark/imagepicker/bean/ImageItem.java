package com.gallopmark.imagepicker.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ImageItem implements Parcelable {
    private String path;
    private long time;
    private String name;
    private String mimeType;

    public ImageItem(String path) {
        this.path = path;
    }

    public ImageItem(String path, long time, String name, String mimeType) {
        this.path = path;
        this.time = time;
        this.name = name;
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isGif() {
        return "image/gif".equals(mimeType);
    }
    /* 图片的路径和创建时间相同就认为是同一张图片*/

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof ImageItem) {
            return path.equals(((ImageItem) obj).path) && name.equals(((ImageItem) obj).name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 + (!TextUtils.isEmpty(path) ? path.hashCode() : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeLong(this.time);
        dest.writeString(this.name);
        dest.writeString(this.mimeType);
    }

    protected ImageItem(Parcel in) {
        this.path = in.readString();
        this.time = in.readLong();
        this.name = in.readString();
        this.mimeType = in.readString();
    }

    public static final Parcelable.Creator<ImageItem> CREATOR = new Parcelable.Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };
}
