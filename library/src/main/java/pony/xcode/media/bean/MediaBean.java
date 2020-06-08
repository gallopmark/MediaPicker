package pony.xcode.media.bean;

import android.net.Uri;
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

    //适配android Q
    private Uri uri;

    public MediaBean() {

    }

    public MediaBean(String path, Uri uri) {
        setPath(path);
        setUri(uri);
    }

    //图片
    public MediaBean(String path, long time, String name, String mimeType, long size, Uri uri) {
        this.path = path;
        this.time = time;
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
        this.uri = uri;
    }

    //视频
    public MediaBean(String path, long time, String name, String mimeType, long duration, long size, Uri uri) {
        this.path = path;
        this.time = time;
        this.name = name;
        this.mimeType = mimeType;
        this.duration = duration;
        this.size = size;
        this.uri = uri;
    }

    protected MediaBean(Parcel in) {
        path = in.readString();
        time = in.readLong();
        name = in.readString();
        mimeType = in.readString();
        duration = in.readLong();
        size = in.readLong();
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<MediaBean> CREATOR = new Creator<MediaBean>() {
        @Override
        public MediaBean createFromParcel(Parcel in) {
            return new MediaBean(in);
        }

        @Override
        public MediaBean[] newArray(int size) {
            return new MediaBean[size];
        }
    };

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setSize(long size) {
        this.size = size;
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

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
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
        dest.writeString(path);
        dest.writeLong(time);
        dest.writeString(name);
        dest.writeString(mimeType);
        dest.writeLong(duration);
        dest.writeLong(size);
        dest.writeParcelable(uri, flags);
    }
}
