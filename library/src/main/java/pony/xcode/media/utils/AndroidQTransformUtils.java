package pony.xcode.media.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;

public class AndroidQTransformUtils {
    /**
     * 解析Android Q版本下视频
     * #耗时操作需要放在子线程中操作
     */
    @TargetApi(Build.VERSION_CODES.Q)
    public static String parseVideoPathToAndroidQ(Context ctx, String path, String mineType) {
        if (path == null) return "";
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            String suffix = MediaUtil.getLastSuffix(mineType);
            File fileDir = ctx.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            if (fileDir == null) {
                return "";
            }
            String filesDir = fileDir.getPath();
            parcelFileDescriptor = ctx.getContentResolver().openFileDescriptor(Uri.parse(path), "r");
            if (parcelFileDescriptor == null) return "";
            FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
            String md5Value = Digest.computeToQMD5(inputStream);
            String fileName;
            if (!TextUtils.isEmpty(md5Value)) {
                fileName = "VID_" + md5Value.toUpperCase() + suffix;
            } else {
                fileName = MediaUtil.getCreateFileName("VID_", suffix);
            }
            String newPath = filesDir + File.separator + fileName;
            File outFile = new File(newPath);
            if (outFile.exists()) {
                return newPath;
            }
            boolean copyFileSuccess = MediaUtil.copyFile(inputStream, outFile);
            if (copyFileSuccess) {
                return newPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MediaUtil.close(parcelFileDescriptor);
        }
        return "";
    }

    /**
     * 解析Android Q版本下图片
     * #耗时操作需要放在子线程中操作
     */
    @TargetApi(Build.VERSION_CODES.Q)
    public static String parseImagePathToAndroidQ(Context ctx, String path, String mineType) {
        if (path == null) return "";
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = ctx.getContentResolver().openFileDescriptor(Uri.parse(path), "r");
            if (parcelFileDescriptor == null) return "";
            FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
            String md5Value = Digest.computeToQMD5(inputStream);
            String suffix = MediaUtil.getLastSuffix(mineType);
            File fileDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (fileDir == null) return "";
            String filesDir = fileDir.getPath();
            String fileName;
            if (!TextUtils.isEmpty(md5Value)) {
                fileName = "IMG_" + md5Value.toUpperCase() + suffix;
            } else {
                fileName = MediaUtil.getCreateFileName("IMG_", suffix);
            }
            String newPath = filesDir + File.separator + fileName;
            File outFile = new File(newPath);
            if (outFile.exists()) {
                return newPath;
            }
            boolean copyFileSuccess = MediaUtil.copyFile(inputStream, outFile);
            if (copyFileSuccess) {
                return newPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MediaUtil.close(parcelFileDescriptor);
        }
        return "";
    }


    /**
     * 复制一份至自己应用沙盒内
     */
    public static String getPathToAndroidQ(Context context, String path, String mimeType) {
        if (MediaUtil.eqVideo(mimeType)) {
            return parseVideoPathToAndroidQ(context.getApplicationContext(), path, mimeType);
        } else {
            return parseImagePathToAndroidQ(context.getApplicationContext(), path, mimeType);
        }
    }

    @Nullable
    public static String getMimeType(Context context, Uri uri) {
        return context.getContentResolver().getType(uri);
    }
}
