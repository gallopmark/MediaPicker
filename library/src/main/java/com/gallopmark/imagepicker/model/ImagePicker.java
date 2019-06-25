package com.gallopmark.imagepicker.model;

import android.app.Activity;
import androidx.fragment.app.Fragment;

import com.gallopmark.imagepicker.ClipImageActivity;
import com.gallopmark.imagepicker.ImagePickerActivity;
import com.gallopmark.imagepicker.loader.ImageLoader;

import java.util.ArrayList;

public class ImagePicker {

    public static final int DEFAULT_REQUEST_CODE = 996;
    /**
     * 图片选择的结果
     */
    public static final String SELECT_RESULT = "select_result";

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
    public static final String SELECTED = "mSelected";
    //初始位置
    public static final String POSITION = "position";

    public static final String IS_CONFIRM = "is_confirm";

    /*图片预览请求码*/
    public static final int PREVIEW_RESULT_CODE = 996;


    private boolean isCrop = false;
    private boolean isUseCamera = true;
    private boolean isSingle = false;
    private boolean isViewImage = true;
    private int mMaxSelectCount;
    private ArrayList<String> mSelected;

    private ImagePicker() {

    }

    ImagePicker(ImagePickerBuilder builder) {
        this.isCrop = builder.isCrop;
        this.isUseCamera = builder.isUseCamera;
        this.isSingle = builder.isSingle;
        this.isViewImage = builder.isViewImage;
        this.mMaxSelectCount = builder.mMaxSelectCount;
        this.mSelected = builder.selected;
        ImageSource.getInstance().setDisplacer(builder.mImageLoader);
    }

    public static ImagePickerBuilder builder() {
        return new ImagePickerBuilder();
    }

    public static class ImagePickerBuilder {

        private boolean isCrop = false;
        private boolean isUseCamera = true;
        private boolean isSingle = false;
        private boolean isViewImage = true;
        private int mMaxSelectCount;
        private ArrayList<String> selected;
        private ImageLoader mImageLoader;

        /**
         * 是否使用图片剪切功能。默认false。如果使用了图片剪切功能，相册只能单选。
         */
        public ImagePickerBuilder isCrop(boolean isCrop) {
            this.isCrop = isCrop;
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
        public ImagePickerBuilder isViewImage(boolean isViewImage) {
            this.isViewImage = isViewImage;
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

        public ImagePicker create() {
            return new ImagePicker(this);
        }
    }

    public void start(Activity activity) {
        start(activity, DEFAULT_REQUEST_CODE);
    }

    /**
     * 打开相册
     */
    public void start(Activity activity, int requestCode) {
        if (isCrop) {
            ClipImageActivity.openActivity(activity, requestCode, isViewImage, isUseCamera, mSelected);
        } else {
            ImagePickerActivity.openActivity(activity, requestCode, isSingle, isViewImage, isUseCamera, mMaxSelectCount, mSelected);
        }
    }

    public void start(Fragment fragment) {
        start(fragment, DEFAULT_REQUEST_CODE);
    }

    /**
     * 打开相册
     */
    public void start(Fragment fragment, int requestCode) {
        if (isCrop) {
            ClipImageActivity.openActivity(fragment, requestCode, isViewImage, isUseCamera, mSelected);
        } else {
            ImagePickerActivity.openActivity(fragment, requestCode, isSingle, isViewImage, isUseCamera, mMaxSelectCount, mSelected);
        }
    }

    public void start(android.app.Fragment fragment) {
        start(fragment, DEFAULT_REQUEST_CODE);
    }

    public void start(android.app.Fragment fragment, int requestCode) {
        if (isCrop) {
            ClipImageActivity.openActivity(fragment, requestCode, isViewImage, isUseCamera, mSelected);
        } else {
            ImagePickerActivity.openActivity(fragment, requestCode, isSingle, isViewImage, isUseCamera, mMaxSelectCount, mSelected);
        }
    }
}
