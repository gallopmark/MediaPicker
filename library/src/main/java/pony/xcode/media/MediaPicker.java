package pony.xcode.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import pony.xcode.media.loader.DefaultImageLoader;
import pony.xcode.media.loader.ImageLoader;

import java.util.ArrayList;

public class MediaPicker {

    public static final int DEFAULT_REQUEST_CODE = 996;
    /**
     * 图片选择的结果
     */
    public static final String SELECT_RESULT = "select_result";

    public static final String CHOOSE_MODE = "choose_mode";
    /**
     * 是否是来自于相机拍照的图片，
     * 只有本次调用相机拍出来的照片，返回时才为true。
     * 当为true时，图片返回当结果有且只有一张图片。
     */
    public static final String IS_CAMERA_IMAGE = "is_camera_image";

    //最大的图片选择数
    public static final String MAX_SELECT_COUNT = "max_select_count";
    //是否单选
    public static final String IS_SINGLE = "is_single";
    //是否点击放大图片查看
    public static final String IS_VIEW_IMAGE = "is_view_image";
    //是否使用拍照功能
    public static final String USE_CAMERA = "is_camera";
    //原来已选择的图片
    public static final String SELECTED = "is_selected";
    //初始位置
    public static final String POSITION = "position";

    public static final String IS_CONFIRM = "is_confirm";

    //视频录制时长
    public static final String DURATION_LIMIT = "duration_limit";
    //视频录制质量
    public static final String VIDEO_QUALITY = "video_quality";
    //视频录制大小
    public static final String SIZE_LIMIT = "size_limit";

    /*图片预览请求码*/
    public static final int PREVIEW_RESULT_CODE = 996;

    private static int mTitleHeight;  //设置标题高度
    private static ImageLoader mImageLoader; //图片加载器
    private boolean isUseCamera = true;
    private boolean isSingle = false;
    private boolean isClickPreview = true;
    private int mMaxSelectCount;
    private ArrayList<String> mSelected;
    private static final int MODE_IMAGE = 1; //图片模式
    private static final int MODE_VIDEO = 2;  //视频模式
    private int mChooseMode = MODE_IMAGE;

    private int mDurationLimit;  //视频录制时长限制 单位为秒 s
    private int mVideoQuality;  //视频拍摄质量 只能是 0和1 不存在中间0.5
    private long mSizeLimit; //设置获取视频文件的大小，以字节为单位．

    private MediaPicker(ImagePickerBuilder builder) {
        init(builder);
    }

    private MediaPicker(VideoPickerBuilder builder) {
        init(builder);
        this.mDurationLimit = builder.mDurationLimit;
        this.mVideoQuality = builder.mVideoQuality;
        this.mSizeLimit = builder.mSizeLimit;
    }

    private void init(BaseBuilder builder) {
        mTitleHeight = builder.mTitleHeight;
        this.mChooseMode = builder.mChooseMode;
        this.isUseCamera = builder.isUseCamera;
        this.isSingle = builder.isSingle;
        this.isClickPreview = builder.isClickPreview;
        this.mMaxSelectCount = builder.mMaxSelectCount;
        this.mSelected = builder.selected;
        setDisplacer(builder.mImageLoader);
    }

    public static int getTitleHeight() {
        return mTitleHeight;
    }

    private static void setDisplacer(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    @NonNull
    public static ImageLoader getDisplacer() {
        if (mImageLoader == null) {
            return new DefaultImageLoader();
        } else {
            return mImageLoader;
        }
    }

    public static void gcDisplacer() {
        if (mImageLoader != null) {
            mImageLoader = null;
        }
    }

    public static ImagePickerBuilder builder() {
        return new ImagePickerBuilder();
    }

    public static VideoPickerBuilder videoBuilder() {
        return new VideoPickerBuilder();
    }

    static class BaseBuilder {
        int mTitleHeight;
        int mChooseMode;
        boolean isUseCamera = true;
        boolean isSingle = false;
        boolean isClickPreview = true;
        int mMaxSelectCount;
        ArrayList<String> selected;
        ImageLoader mImageLoader;
    }

    public static class ImagePickerBuilder extends BaseBuilder {

        public ImagePickerBuilder() {
            mChooseMode = MODE_IMAGE;
        }

        /**
         * 设置标题高度
         */
        public ImagePickerBuilder titleHeight(int titleHeight) {
            this.mTitleHeight = titleHeight;
            return this;
        }

        /**
         * 是否单选
         */
        public ImagePickerBuilder isSingle(boolean isSingle) {
            this.isSingle = isSingle;
            return this;
        }

        /**
         * 是否点击放大图片查看,，默认为true
         */
        public ImagePickerBuilder isClickPreview(boolean isViewImage) {
            this.isClickPreview = isViewImage;
            return this;
        }

        /**
         * 是否使用拍照功能。
         */
        public ImagePickerBuilder isUseCamera(boolean useCamera) {
            this.isUseCamera = useCamera;
            return this;
        }

        /**
         * 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
         */
        public ImagePickerBuilder maxSelectCount(int maxSelectCount) {
            this.mMaxSelectCount = maxSelectCount;
            return this;
        }

        /**
         * 接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
         * 选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
         */
        public ImagePickerBuilder selected(ArrayList<String> selected) {
            this.selected = selected;
            return this;
        }

        /**
         * 设置图片加载器
         */
        public ImagePickerBuilder imageLoader(ImageLoader imageLoader) {
            this.mImageLoader = imageLoader;
            return this;
        }

        public MediaPicker create() {
            return new MediaPicker(this);
        }

        public void start(Activity activity) {
            start(activity, DEFAULT_REQUEST_CODE);
        }

        public void start(Activity activity, int requestCode) {
            create().start(activity, requestCode);
        }

        public void start(Fragment fragment) {
            start(fragment, DEFAULT_REQUEST_CODE);
        }

        public void start(Fragment fragment, int requestCode) {
            create().start(fragment, requestCode);
        }

        public void start(android.app.Fragment fragment) {
            start(fragment, DEFAULT_REQUEST_CODE);
        }

        public void start(android.app.Fragment fragment, int requestCode) {
            create().start(fragment, requestCode);
        }
    }

    public static class VideoPickerBuilder extends BaseBuilder {
        private int mDurationLimit;
        private int mVideoQuality = 1;
        private long mSizeLimit;

        public VideoPickerBuilder() {
            mChooseMode = MODE_VIDEO;
        }

        public VideoPickerBuilder titleHeight(int titleHeight) {
            this.mTitleHeight = titleHeight;
            return this;
        }

        /**
         * 是否单选
         */
        public VideoPickerBuilder isSingle(boolean isSingle) {
            this.isSingle = isSingle;
            return this;
        }

        /**
         * 是否点击放大图片查看,，默认为true
         */
        public VideoPickerBuilder isClickPreview(boolean isViewImage) {
            this.isClickPreview = isViewImage;
            return this;
        }

        /**
         * 是否使用拍照功能。
         */
        public VideoPickerBuilder isUseCamera(boolean useCamera) {
            this.isUseCamera = useCamera;
            return this;
        }

        public VideoPickerBuilder durationLimit(int duration) {
            this.mDurationLimit = duration;
            return this;
        }

        /*只能设置0或1，不存在中间0.5*/
        public VideoPickerBuilder videoQuality(int quality) {
            this.mVideoQuality = quality;
            return this;
        }

        public VideoPickerBuilder sizeLimit(int sizeLimit) {
            this.mSizeLimit = sizeLimit;
            return this;
        }

        /**
         * 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
         */
        public VideoPickerBuilder maxSelectCount(int maxSelectCount) {
            this.mMaxSelectCount = maxSelectCount;
            return this;
        }

        /**
         * 接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
         * 选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
         */
        public VideoPickerBuilder selected(ArrayList<String> selected) {
            this.selected = selected;
            return this;
        }

        /**
         * 设置图片加载器
         */
        public VideoPickerBuilder imageLoader(ImageLoader imageLoader) {
            this.mImageLoader = imageLoader;
            return this;
        }

        public MediaPicker create() {
            return new MediaPicker(this);
        }

        public void start(Activity activity) {
            start(activity, DEFAULT_REQUEST_CODE);
        }

        public void start(Activity activity, int requestCode) {
            create().start(activity, requestCode);
        }

        public void start(Fragment fragment) {
            start(fragment, DEFAULT_REQUEST_CODE);
        }

        public void start(Fragment fragment, int requestCode) {
            create().start(fragment, requestCode);
        }

        public void start(android.app.Fragment fragment) {
            start(fragment, DEFAULT_REQUEST_CODE);
        }

        public void start(android.app.Fragment fragment, int requestCode) {
            create().start(fragment, requestCode);
        }
    }

    public void start(Activity activity) {
        start(activity, DEFAULT_REQUEST_CODE);
    }

    /**
     * 打开相册
     */
    public void start(Activity activity, int requestCode) {
        int mode = MediaConfig.MODE_IMAGE;
        if (mChooseMode == MODE_VIDEO) {
            mode = MediaConfig.MODE_VIDEO;
        }
        MediaPickerActivity.openActivity(activity, requestCode,
                isSingle, isClickPreview,
                isUseCamera, mMaxSelectCount,
                mSelected, mDurationLimit,
                mVideoQuality, mSizeLimit,
                mode);
    }

    public void start(Fragment fragment) {
        start(fragment, DEFAULT_REQUEST_CODE);
    }

    /**
     * 打开相册
     */
    public void start(Fragment fragment, int requestCode) {
        int mode = MediaConfig.MODE_IMAGE;
        if (mChooseMode == MODE_VIDEO) {
            mode = MediaConfig.MODE_VIDEO;
        }
        MediaPickerActivity.openActivity(fragment, requestCode,
                isSingle, isClickPreview,
                isUseCamera, mMaxSelectCount,
                mSelected, mDurationLimit,
                mVideoQuality, mSizeLimit,
                mode);
    }

    public void start(android.app.Fragment fragment) {
        start(fragment, DEFAULT_REQUEST_CODE);
    }

    public void start(android.app.Fragment fragment, int requestCode) {
        int mode = MediaConfig.MODE_IMAGE;
        if (mChooseMode == MODE_VIDEO) {
            mode = MediaConfig.MODE_VIDEO;
        }
        MediaPickerActivity.openActivity(fragment, requestCode,
                isSingle, isClickPreview,
                isUseCamera, mMaxSelectCount,
                mSelected, mDurationLimit,
                mVideoQuality, mSizeLimit,
                mode);
    }

    public static void startAppSettings(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
            activity.startActivity(intent);
        } catch (Exception ignored) {
        }
    }
}
