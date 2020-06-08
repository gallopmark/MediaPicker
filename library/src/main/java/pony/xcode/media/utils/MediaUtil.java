package pony.xcode.media.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import pony.xcode.media.MediaConfig;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    public static boolean isExternalStorageDocument(@NonNull Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    public static boolean isDownloadsDocument(@NonNull Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    public static boolean isMediaDocument(@NonNull Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(@NonNull Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, @NonNull Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @Nullable
    public static String getPath(final Context context, final Uri uri) {
        if (uri == null) return null;
        // DocumentProvider
        if (SDKVersionUtils.isKitkatAbove() && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    if (SDKVersionUtils.isAndroidQAbove()) {
                        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + split[1];
                    } else {
                        //noinspection deprecation
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                try {
                    long _id = Long.parseLong(id);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), _id);
                    return getDataColumn(context, contentUri, null, null);
                } catch (Exception e) {
                    return null;
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                if (contentUri != null) {
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
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
     * 获取后缀
     */
    public static String getLastSuffix(String mineType) {
        String defaultSuffix = MediaConfig.PNG;
        try {
            int index = mineType.lastIndexOf("/") + 1;
            if (index > 0) {
                return "." + mineType.substring(index);
            }
        } catch (Exception e) {
            return defaultSuffix;
        }
        return defaultSuffix;
    }

    /**
     * Copies one file into the other with the given paths.
     * In the event that the paths are the same, trying to copy one file to the other
     * will cause both files to become null.
     * Simply skipping this step if the paths are identical.
     */
    public static boolean copyFile(FileInputStream fileInputStream, File outFile) throws IOException {
        if (fileInputStream == null) {
            return false;
        }
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputChannel = fileInputStream.getChannel();
            fileOutputStream = new FileOutputStream(outFile);
            outputChannel = fileOutputStream.getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            fileInputStream.close();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (inputChannel != null) {
                inputChannel.close();
            }
            if (outputChannel != null) {
                outputChannel.close();
            }
        }
    }

    public static void close(@Nullable Closeable c) {
        // java.lang.IncompatibleClassChangeError: interface not implemented
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                // silence
            }
        }
    }

    /**
     * 路径转Uri
     * @param context 上下文
     * @param path 文件路径
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
