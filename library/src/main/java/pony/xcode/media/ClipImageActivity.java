package pony.xcode.media;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.FrameLayout;

import pony.xcode.media.utils.ImageUtil;
import pony.xcode.media.utils.StringUtils;
import pony.xcode.media.widget.ClipImageView;

import java.io.File;
import java.util.ArrayList;

public class ClipImageActivity extends MediaBaseActivity {
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
        bundle.putBoolean(MediaSelector.IS_VIEW_IMAGE, isViewImage);
        bundle.putBoolean(MediaSelector.USE_CAMERA, useCamera);
        bundle.putStringArrayList(MediaSelector.SELECTED, selected);
        return bundle;
    }

    @Override
    protected int bindContentView() {
        return R.layout.mediaselector_activity_clip_image;
    }

    @Override
    protected void setup(@Nullable Bundle savedInstanceState) {
        initIntent();
        initView();
    }

    private void initIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            MediaPickerActivity.openActivity(this, mRequestCode, true, true, true, 0, null, 0, 0, 0, MediaConfig.MODE_IMAGE);
        } else {
            mRequestCode = bundle.getInt("requestCode", 0);
            MediaPickerActivity.openActivity(this, mRequestCode, true,
                    bundle.getBoolean(MediaSelector.IS_VIEW_IMAGE, true),
                    bundle.getBoolean(MediaSelector.USE_CAMERA, true), 0,
                    bundle.getStringArrayList(MediaSelector.SELECTED), 0, 0, 0,
                    MediaConfig.MODE_IMAGE);
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
            ArrayList<String> images = data.getStringArrayListExtra(MediaSelector.SELECT_RESULT);
            if (images == null || images.isEmpty()) return;
            isCameraImage = data.getBooleanExtra(MediaSelector.IS_CAMERA_IMAGE, false);
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
            intent.putStringArrayListExtra(MediaSelector.SELECT_RESULT, selectImages);
            intent.putExtra(MediaSelector.IS_CAMERA_IMAGE, isCameraImage);
            setResult(RESULT_OK, intent);
        }
        finish();
    }
}
