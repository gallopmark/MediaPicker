package com.gallopmark.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.FrameLayout;

import com.gallopmark.imagepicker.model.ImagePicker;
import com.gallopmark.imagepicker.utils.ImageUtil;
import com.gallopmark.imagepicker.utils.StringUtils;
import com.gallopmark.imagepicker.widget.ClipImageView;

import java.io.File;
import java.util.ArrayList;

public class ClipImageActivity extends BaseActivity {
    private ClipImageView mClipImageView;
    private int mRequestCode;
    private boolean isCameraImage;

    public static void openActivity(Activity context, int requestCode, boolean isViewImage,
                                    boolean useCamera, ArrayList<String> selected) {
        Intent intent = new Intent(context, ClipImageActivity.class);
        intent.putExtras(dataPackages(requestCode, isViewImage, useCamera, selected));
        context.startActivityForResult(intent, requestCode);
    }

    public static void openActivity(Fragment fragment, int requestCode, boolean isViewImage,
                                    boolean useCamera, ArrayList<String> selected) {
        if (fragment.getActivity() == null) return;
        Intent intent = new Intent(fragment.getContext(), ClipImageActivity.class);
        intent.putExtras(dataPackages(requestCode, isViewImage, useCamera, selected));
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void openActivity(android.app.Fragment fragment, int requestCode, boolean isViewImage,
                                    boolean useCamera, ArrayList<String> selected) {
        if (fragment.getActivity() == null) return;
        Intent intent = new Intent(fragment.getActivity(), ClipImageActivity.class);
        intent.putExtras(dataPackages(requestCode, isViewImage, useCamera, selected));
        fragment.startActivityForResult(intent, requestCode);
    }

    private static Bundle dataPackages(int requestCode, boolean isViewImage, boolean useCamera,
                                       ArrayList<String> selected) {
        Bundle bundle = new Bundle();
        bundle.putInt("requestCode", requestCode);
        bundle.putBoolean(ImagePicker.IS_VIEW_IMAGE, isViewImage);
        bundle.putBoolean(ImagePicker.USE_CAMERA, useCamera);
        bundle.putStringArrayList(ImagePicker.SELECTED, selected);
        return bundle;
    }

    @Override
    protected int bindContentView() {
        return R.layout.activity_clip_image;
    }

    @Override
    protected void setup(@Nullable Bundle savedInstanceState) {
        initIntent();
        initView();
    }

    private void initIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            ImagePickerActivity.openActivity(this, mRequestCode, true, true, true, 0, null);
        } else {
            mRequestCode = bundle.getInt("requestCode", 0);
            ImagePickerActivity.openActivity(this, mRequestCode, true,
                    bundle.getBoolean(ImagePicker.IS_VIEW_IMAGE, true),
                    bundle.getBoolean(ImagePicker.USE_CAMERA, true), 0,
                    bundle.getStringArrayList(ImagePicker.SELECTED));
        }
    }

    private void initView() {
        Toolbar toolBar = findViewById(R.id.toolBar);
        final FrameLayout mConfirmLayout = findViewById(R.id.mConfirmLayout);
        mClipImageView = findViewById(R.id.mClipImageView);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mConfirmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClipImageView.getDrawable() != null) {
                    mConfirmLayout.setEnabled(false);
                    confirm(mClipImageView.clipImage());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mRequestCode && data != null) {
            ArrayList<String> images = data.getStringArrayListExtra(ImagePicker.SELECT_RESULT);
            if (images == null || images.isEmpty()) return;
            isCameraImage = data.getBooleanExtra(ImagePicker.IS_CAMERA_IMAGE, false);
            Bitmap bitmap = ImageUtil.decodeSampledBitmapFromFile(images.get(0), 720, 1080);
            if (bitmap != null) {
                mClipImageView.setBitmapData(bitmap);
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void confirm(Bitmap bitmap) {
        String imagePath = null;
        if (bitmap != null) {
            String savePath = getExternalCacheDir() == null ? getCacheDir().getPath() : getExternalCacheDir().getPath();
            imagePath = ImageUtil.saveImage(bitmap, savePath + File.separator + "image_select");
            bitmap.recycle();
        }
        if (StringUtils.isNotEmptyString(imagePath)) {
            ArrayList<String> selectImages = new ArrayList<>();
            selectImages.add(imagePath);
            Intent intent = new Intent();
            intent.putStringArrayListExtra(ImagePicker.SELECT_RESULT, selectImages);
            intent.putExtra(ImagePicker.IS_CAMERA_IMAGE, isCameraImage);
            setResult(RESULT_OK, intent);
        }
        finish();
    }
}
