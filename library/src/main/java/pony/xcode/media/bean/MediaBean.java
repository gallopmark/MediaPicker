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
    private String realPath;
    private Uri uri;
    @SuppressWarnings("WeakerAccess")
    String uriPath;

    public MediaBean() {

    }

    public MediaBean(String path, String realPath) {
        this.path = path;
        this.realPath = realPath;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public String getRealPath() {
        return realPath;
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

    public boolean isUriPath() {
        return !TextUtils.isEmpty(path) && path.contains("content://");
    }

    public void setUri(Uri uri) {
        this.uri = uri;
        this.uriPath = this.uri.toString();
    }

    public Uri getUri() {
        return uri;
    }

    public String getUriPath() {
        if (uri == null) return "";
        return uri.toString();
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
        dest.writeString(this.realPath);
        dest.writeString(this.getUriPath());
    }

    protected MediaBean(Parcel in) {
        this.path = in.readString();
        this.realPath = in.readString();
        this.uriPath = in.readString();
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
