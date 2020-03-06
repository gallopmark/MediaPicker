package pony.xcode.media.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import pony.xcode.media.utils.DateUtils;

public class MediaBean implements Parcelable {
    private String path;
    private long time;
    private String name;
    private String mimeType;
    private long duration;
    private long size;

    public MediaBean(String path) {
        this.path = path;
    }

    public MediaBean(String path, long time, String name, String mimeType) {
        this.path = path;
        this.time = time;
        this.name = name;
        this.mimeType = mimeType;
    }

    public MediaBean(String path, long time, String name, String mimeType, long duration, long size) {
        this.path = path;
        this.time = time;
        this.name = name;
        this.mimeType = mimeType;
        this.duration = duration;
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public long getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public String getFormatDurationTime() {
        return DateUtils.generateTime(duration);
    }

    public boolean isGif() {
        return "image/gif".equals(mimeType);
    }

    /* 图片的路径和创建时间相同就认为是同一张图片*/
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof MediaBean) {
            return path.equals(((MediaBean) obj).path) && name.equals(((MediaBean) obj).name);
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

    protected MediaBean(Parcel in) {
        this.path = in.readString();
        this.time = in.readLong();
        this.name = in.readString();
        this.mimeType = in.readString();
    }

    public static final Parcelable.Creator<MediaBean> CREATOR = new Parcelable.Creator<MediaBean>() {
        @Override
        public MediaBean createFromParcel(Parcel source) {
            return new MediaBean(source);
        }

        @Override
        public MediaBean[] newArray(int size) {
            return new MediaBean[size];
        }
    };
}
