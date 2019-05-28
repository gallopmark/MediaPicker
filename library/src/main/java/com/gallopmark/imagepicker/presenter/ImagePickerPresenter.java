package com.gallopmark.imagepicker.presenter;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gallopmark.imagepicker.R;
import com.gallopmark.imagepicker.bean.ImageFolder;
import com.gallopmark.imagepicker.bean.ImageItem;
import com.gallopmark.imagepicker.model.ImageSource;
import com.gallopmark.imagepicker.utils.DateUtils;
import com.gallopmark.imagepicker.model.ImagePicker;
import com.gallopmark.imagepicker.utils.ImageUtil;
import com.gallopmark.imagepicker.view.ImagePickerView;

import java.io.File;
import java.util.ArrayList;

public class ImagePickerPresenter {

    private Activity activity;
    private ImagePickerView pickerView;

    private static final int PERMISSION_WRITE_EXTERNAL_REQUEST_CODE = 0x00000011;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;

    private static final int CAMERA_REQUEST_CODE = 0x00000010;

    private static final int MESSAGE_UPDATE_CODE = 0x0001;

    private boolean isLoadImage = false;

    private boolean isShowTime;

    private MyHandler handler;
    private ArrayList<ImageFolder> mFolders;
    private String mPhotoPath;

    private HideRunnable mHide;

    private class MyHandler extends Handler {
        private MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_UPDATE_CODE) {
                pickerView.onLoadFolders(mFolders);
            }
        }
    }

    private class HideRunnable implements Runnable {
        private TextView mTimeTextView;

        HideRunnable(TextView mTimeTextView) {
            this.mTimeTextView = mTimeTextView;
        }

        @Override
        public void run() {
            hideTime(mTimeTextView);
        }
    }

    public ImagePickerPresenter(Activity activity, @NonNull ImagePickerView pickerView) {
        this.activity = activity;
        this.pickerView = pickerView;
        handler = new MyHandler(Looper.getMainLooper());
    }

    public void initView() {
        pickerView.onGetIntent();
        pickerView.onInitView();
        pickerView.onInitListener();
        pickerView.onInitImageList();
        pickerView.onCheckExternalPermission();
    }

    public void checkPermissionAndLoadImages() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasExternalPermission()) { //有权限，加载图片。
                loadImageFromSDCard();
            } else { //没有权限，申请权限。
                requestExternalPermission();
            }
        } else {
            loadImageFromSDCard();
        }
    }

    private void loadImageFromSDCard() {
        ImageSource.loadImageForSDCard(activity, new ImageSource.OnGetImageCallback() {
            @Override
            public void onSuccess(ArrayList<ImageFolder> folders) {
                mFolders = folders;
                handler.sendEmptyMessage(MESSAGE_UPDATE_CODE);
            }
        });
        isLoadImage = true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasExternalPermission() {
        /*android 6.0 以上需要动态申请权限*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestExternalPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_REQUEST_CODE);
    }

    public void changeTime(TextView mTimeTextView, ImageItem image) {
        if (image != null) {
            String time = DateUtils.getImageTime(activity, image.getTime() * 1000);
            mTimeTextView.setText(time);
            showTime(mTimeTextView);
            if (mHide == null) {
                mHide = new HideRunnable(mTimeTextView);
            } else {
                handler.removeCallbacks(mHide);
            }
            handler.postDelayed(mHide, 1500);
        }
    }

    /*隐藏时间条*/
    private void hideTime(TextView mTimeTextView) {
        if (isShowTime) {
            ObjectAnimator.ofFloat(mTimeTextView, "alpha", 1, 0).setDuration(300).start();
            isShowTime = false;
        }
    }

    /*显示时间条*/
    private void showTime(TextView mTimeTextView) {
        if (!isShowTime) {
            ObjectAnimator.ofFloat(mTimeTextView, "alpha", 0, 1).setDuration(300).start();
            isShowTime = true;
        }
    }

    /*弹出文件夹列表*/
    public void openFolder(final FrameLayout mFolderLayout, final FrameLayout mFolderNameLayout) {
        if (mFolderLayout.getVisibility() != View.VISIBLE) {
            mFolderNameLayout.setEnabled(false);
            ObjectAnimator animator = ObjectAnimator.ofFloat(mFolderLayout, "translationY",
                    mFolderLayout.getHeight(), 0).setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mFolderLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mFolderNameLayout.setEnabled(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mFolderNameLayout.setEnabled(true);
                }
            });
            animator.start();
        }
    }

    /*收起文件夹列表*/
    public void closeFolder(final FrameLayout mFolderLayout, final FrameLayout mFolderNameLayout) {
        if (mFolderLayout.getVisibility() != View.GONE) {
            mFolderNameLayout.setEnabled(false);
            ObjectAnimator animator = ObjectAnimator.ofFloat(mFolderLayout, "translationY",
                    0, mFolderLayout.getHeight()).setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mFolderLayout.setVisibility(View.GONE);
                    mFolderNameLayout.setEnabled(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mFolderNameLayout.setEnabled(true);
                }
            });
            animator.start();
        }
    }

    public void checkPermissionAndCamera() {
        if (hasCameraPermission()) { //有调起相机拍照。
            openCamera();
        } else { //没有权限，申请权限。
            requestCameraPermission();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，加载图片。
                loadImageFromSDCard();
            } else {
                //拒绝权限，弹出提示框。
                if (permissions.length > 0 && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
                    afterDenied(1, activity.getString(R.string.imagePicker_noSdcard), false);
                } else {
                    afterDenied(2, activity.getString(R.string.imagePicker_dismiss_sdcard), false);
                }
            }
        } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机拍照。
                openCamera();
            } else {
                if (permissions.length > 0 && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
                    afterDenied(1, activity.getString(R.string.imagePicker_noCamera), true);
                } else {
                    afterDenied(2, activity.getString(R.string.imagePicker_dismiss_camera), true);
                }
            }
        }
    }

    /*调起相机拍照*/
    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(activity.getPackageManager()) != null) {
            try {
                File photoFile = ImageUtil.createCameraFile();
                if (photoFile != null) {
                    //通过FileProvider创建一个content类型的Uri
                    Uri photoUri = ImageUtil.getImageUri(activity, photoFile);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    activity.startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
                    mPhotoPath = photoFile.getAbsolutePath();
                }
            } catch (Exception e) {
                mPhotoPath = null;
            }
        }
    }

    private void afterDenied(final int type, String message, final boolean isCamera) {
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(activity.getString(R.string.imagePicker_dialog_title))
                .setMessage(message)
                .setPositiveButton(activity.getString(R.string.imagePicker_dialog_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        if (type == 1) {
                            startAppSettings();
                        } else {
                            if (isCamera) {
                                requestCameraPermission();
                            } else {
                                requestExternalPermission();
                            }
                        }
                    }
                }).setNegativeButton(activity.getString(R.string.imagePicker_dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                if (!isCamera) {
                    activity.finish();
                }
            }
        }).show();
    }

    /* 启动应用的设置*/
    private void startAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    /*拍照成功返回路径*/
    private String getPhotoPath() {
        return mPhotoPath;
    }

    public boolean isLoadImage() {
        return isLoadImage;
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String mPhotoPath = getPhotoPath();
                if (mPhotoPath == null || !new File(mPhotoPath).exists()) return;
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(mPhotoPath))));
                ArrayList<String> images = new ArrayList<>();
                images.add(mPhotoPath);
                setResult(Activity.RESULT_OK, images, true);
                activity.finish();
            }
        }
    }

    public void setResult(int resultCode, ArrayList<String> images, boolean isCameraImage) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ImagePicker.SELECT_RESULT, images);
        intent.putExtra(ImagePicker.IS_CAMERA_IMAGE, isCameraImage);
        activity.setResult(resultCode, intent);
    }

    public void onReStart() {
        if (!isLoadImage && hasExternalPermission()) {
            loadImageFromSDCard();
        }
    }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
    }
}
