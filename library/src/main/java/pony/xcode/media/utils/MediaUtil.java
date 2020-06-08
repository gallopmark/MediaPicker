package pony.xcode.media.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import pony.xcode.media.MediaConfig;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MediaUtil {

    private static String toString(long currentTime) {
        return String.valueOf(currentTime);
    }

    /**
     * 检查图片是否存在。ContentResolver查询处理的数据有可能文件路径并不存在。
     */
    public static boolean isFileExists(String filePath) {
        if (TextUtils.isEmpty(filePath)) return false;
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }

    public static File createImageFile(Context context) {
        return createCaptureFile(context, 1, getCreateFileName("IMG_", MediaConfig.JPEG));
    }

    public static File createVideoFile(Context context) {
        return createCaptureFile(context, 2, getCreateFileName("VID_", MediaConfig.MP4));
    }

    /**
     * 文件根目录
     */
    @Nullable
    private static File getRootDirFile(Context context, int type) {
        if (type == 2) {
            return context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        }
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    private static File createCaptureFile(Context context, int type, String child) {
        String state = Environment.getExternalStorageState();
        File parent = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) : getRootDirFile(context, type);
        if (parent == null) return null;
        try {
            boolean isCreated;
            if (!parent.exists()) {
                isCreated = parent.mkdirs();
            } else {
                isCreated = true;
            }
            if (!isCreated) return null;
            File file = new File(parent, child);
            if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file))) {
                return null;
            }
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public static Uri getProviderUri(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = context.getPackageName() + ".provider";
            uri = FileProvider.getUriForFile(context, authority, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.Q)
    public static Uri createImageUri(final Context context) {
        String status = Environment.getExternalStorageState();
        String time = toString(System.currentTimeMillis());
        // ContentValues是我们希望这条记录被创建时包含的数据信息
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, getCreateFileName("IMG_", MediaConfig.JPEG));
        values.put(MediaStore.Images.Media.DATE_TAKEN, time);
        values.put(MediaStore.Images.Media.MIME_TYPE, MediaConfig.MIME_TYPE_IMAGE);
        Uri imageUri;
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, MediaConfig.RELATIVE_PATH);
            imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            imageUri = context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
        return imageUri;
    }

    /**
     * 创建一条视频地址uri,用于保存录制的视频
     *
     * @return 视频的uri
     */
    @Nullable
    @TargetApi(Build.VERSION_CODES.Q)
    public static Uri createVideoUri(final Context context) {
        String status = Environment.getExternalStorageState();
        String time = toString(System.currentTimeMillis());
        // ContentValues是我们希望这条记录被创建时包含的数据信息
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.DISPLAY_NAME, getCreateFileName("VID_", MediaConfig.MP4));
        values.put(MediaStore.Video.Media.DATE_TAKEN, time);
        values.put(MediaStore.Video.Media.MIME_TYPE, MediaConfig.MIME_TYPE_VIDEO);
        Uri videoUri;
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, MediaConfig.RELATIVE_PATH);
            videoUri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            videoUri = context.getContentResolver().insert(MediaStore.Video.Media.INTERNAL_CONTENT_URI, values);
        }
        return videoUri;
    }

    public static String getCreateFileName(String prefix, String suffix) {
        long millis = System.currentTimeMillis();
        return prefix + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(millis) + suffix;
    }

    /**
     * 是否是视频
     */
    public static boolean eqVideo(String mimeType) {
        return mimeType != null && mimeType.startsWith(MediaConfig.MIME_TYPE_PREFIX_VIDEO);
    }


    /**
     * 是否是图片
     */
    public static boolean eqImage(String mimeType) {
        return mimeType != null && mimeType.startsWith(MediaConfig.MIME_TYPE_PREFIX_IMAGE);
    }

    /**
     * 路径转Uri
     *
     * @param context 上下文
     * @param path    文件路径
     */
    public static Uri getUri(Context context, String path) {
        Uri contentUri = MediaStore.Files.getContentUri("external");
        Cursor cursor = context.getContentResolver().query(contentUri,
                new String[]{MediaStore.Files.FileColumns._ID}, MediaStore.Files.FileColumns.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
            cursor.close();
            return ContentUris.withAppendedId(contentUri, id);
        } else {
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.DATA, path);
                return context.getContentResolver().insert(contentUri, values);
            } else {
                return null;
            }
        }
    }

}
