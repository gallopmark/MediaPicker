package pony.xcode.media;

/*常量类*/
public class MediaConfig {
    public static final int MODE_IMAGE = 1;
    public static final int MODE_VIDEO = 2;

    public final static String RELATIVE_PATH = "DCIM/Camera";
    public static final String MIME_TYPE_IMAGE = "image/jpeg";
    public final static String MIME_TYPE_VIDEO = "video/mp4";

    public static final String PNG = ".png";
    public final static String JPEG = ".jpg";

    public final static String MP4 = ".mp4";

    public static final long DEFAULT_DURATION_LIMIT = 15;  //视频默认录制时长
    public static final int DEFAULT_VIDEO_QUALITY = 1;  //默认拍摄质量
    public static final long DEFAULT_SIZE_LIMIT = 20 * 1024 * 1024;  //视频默认录制大小

    public final static String MIME_TYPE_PREFIX_IMAGE = "image";
    public final static String MIME_TYPE_PREFIX_VIDEO = "video";

}
