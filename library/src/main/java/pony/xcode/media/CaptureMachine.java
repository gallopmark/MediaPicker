package pony.xcode.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

import pony.xcode.media.utils.DialogUtils;
import pony.xcode.media.utils.FileUtils;
import pony.xcode.media.utils.MediaUtil;
import pony.xcode.media.utils.SDKVersionUtils;

//拍照或录制视频
public class CaptureMachine {

    private Activity mActivity;
    private String mFilePath;
    private Uri mFileUri;
    private int mRequestCode;

    private CaptureMachine(Activity activity) {
        this.mActivity = activity;
    }

    public static CaptureMachine from(Activity activity) {
        return new CaptureMachine(activity);
    }

    //调起系统拍照
    public void startImageCapture(int requestCode) {
        this.mRequestCode = requestCode;
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            Uri imageUri = null;
            if (SDKVersionUtils.isAndroidQAbove()) { //适配android Q
                imageUri = MediaUtil.createImageUri(mActivity.getApplicationContext());
                if (imageUri != null) {
                    mFileUri = imageUri;
                } else {
                    mFilePath = null;
                }
            } else {
                File photoFile = MediaUtil.createImageFile(mActivity.getApplicationContext());
                if (photoFile != null) {
                    //通过FileProvider创建一个content类型的Uri
                    imageUri = MediaUtil.getProviderUri(mActivity, photoFile);
                    mFileUri = imageUri;
                    mFilePath = photoFile.getAbsolutePath();
                }
            }
            if (imageUri == null) {
                DialogUtils.showDialog(mActivity, mActivity.getString(R.string.mediapicker_create_file_error));
            } else {
                try {
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    mActivity.startActivityForResult(captureIntent, requestCode);
                } catch (Exception e) {
                    DialogUtils.showUnusableCamera(mActivity);
                }
            }
        } else {
            DialogUtils.showUnusableCamera(mActivity);
        }
    }

    //调起系统相机录像
    public void startVideoCapture(int requestCode, @NonNull VideoConfig config) {
        this.mRequestCode = requestCode;
        Intent captureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (captureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            Uri videoUri = null;
            if (SDKVersionUtils.isAndroidQAbove()) {
                videoUri = MediaUtil.createVideoUri(mActivity.getApplicationContext());
                if (videoUri != null) {
                    mFileUri = videoUri;
                } else {
                    mFilePath = null;
                }
            } else {
                File videoFile = MediaUtil.createVideoFile(mActivity);
                if (videoFile != null) {
                    //通过FileProvider创建一个content类型的Uri
                    videoUri = MediaUtil.getProviderUri(mActivity, videoFile);
                    mFileUri = videoUri;
                    mFilePath = videoFile.getAbsolutePath();
                }
            }
            if (videoUri != null) {
                try {
                    if (config.getDurationLimit() > 0) {
                        captureIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, config.getDurationLimit());
                    }
                    if (config.getSizeLimit() > 0) {
                        captureIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, config.getSizeLimit());
                    }
                    captureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, config.getQuality());
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                    captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    mActivity.startActivityForResult(captureIntent, requestCode);
                } catch (Exception e) {
                    DialogUtils.showUnusableCamera(mActivity);
                }
            } else {
                DialogUtils.showDialog(mActivity, mActivity.getString(R.string.mediapicker_create_file_error));
            }
        }
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    //在onActivityResult方法里回调
    @Nullable
    public String getFilePath() {
        String filePath;
        if (SDKVersionUtils.isAndroidQAbove()) {
            filePath = FileUtils.getPath(mActivity, mFileUri);
        } else {
            mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(mFilePath))));
            filePath = mFilePath;
        }
        return filePath;
    }

    public Uri getFileUri() {
        return mFileUri;
    }
}
