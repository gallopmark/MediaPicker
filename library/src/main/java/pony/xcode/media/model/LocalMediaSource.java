package pony.xcode.media.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import pony.xcode.media.MediaConfig;
import pony.xcode.media.R;
import pony.xcode.media.bean.MediaFolder;
import pony.xcode.media.bean.MediaBean;
import pony.xcode.media.utils.AndroidQTransformUtils;
import pony.xcode.media.utils.MediaUtil;
import pony.xcode.media.utils.SDKVersionUtils;
import pony.xcode.media.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalMediaSource implements Handler.Callback {

    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
    private static final int MSG_QUERY_MEDIA_SUCCESS = 0;
    private static final int MSG_QUERY_MEDIA_ERROR = -1;

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID, //id
            MediaStore.MediaColumns.DATA,   //路径
            MediaStore.MediaColumns.MIME_TYPE,  //mime type
            MediaStore.MediaColumns.DATE_ADDED, //添加的时间
            MediaStore.MediaColumns.DISPLAY_NAME, //名称
            MediaStore.MediaColumns.DURATION,   //时长
            MediaStore.MediaColumns.SIZE    //大小
    };
    private static final String NOT_GIF = "!='image/gif'";  //过滤掉gif图
    private static final String SELECTION_NOT_GIF = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0"
            + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF;

    private int mChooseMode;
    private Handler mHandler;
    private OnCompleteListener mCompleteListener;

    /**
     * 从SDCard加载图片
     */
    public void loadImageForSDCard(final Context context, final int mode, final OnCompleteListener listener) {
        //由于扫描图片是耗时的操作，所以要在子线程处理。
        mChooseMode = mode;
        mHandler = new Handler(Looper.getMainLooper(), this);
        mCompleteListener = listener;
        mCompleteListener.onPreLoad();
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<MediaBean> images = new ArrayList<>();
                    final Cursor data = context.getContentResolver().query(QUERY_URI, PROJECTION, getSelection(), getSelectionArgs(), ORDER_BY);
                    final boolean isAndroidQ = SDKVersionUtils.isAndroidQAbove();
                    if (data != null) {
                        if (mChooseMode == MediaConfig.MODE_VIDEO) { //获取视频
                            while (data.moveToNext()) {
                                long duration = data.getLong(data.getColumnIndex(PROJECTION[5]));
                                long size = data.getLong(data.getColumnIndex(PROJECTION[6]));
                                if (duration == 0) {
                                    // 时长如果为0，就当做损坏的视频处理过滤掉
                                    continue;
                                }
                                if (size <= 0) {
                                    // 视频大小为0过滤掉
                                    continue;
                                }
                                String path = data.getString(data.getColumnIndex(PROJECTION[1]));
                                String mimeType = data.getString(data.getColumnIndex(PROJECTION[2]));
                                if (isAndroidQ) {  //适配 android Q
                                    long id = data.getLong(data.getColumnIndex(PROJECTION[0]));
                                    path = AndroidQTransformUtils.parseVideoPathToAndroidQ(context, getRealPathAndroid_Q(id), mimeType);
                                }
                                long time = data.getLong(data.getColumnIndex(PROJECTION[3]));
                                String name = data.getString(data.getColumnIndex(PROJECTION[4]));
                                if (!TextUtils.equals(MediaUtil.getExtensionName(path), "downloading") && MediaUtil.isFileExists(path)) {
                                    images.add(new MediaBean(path, time, name, mimeType, duration, size));
                                }
                            }
                        } else {  //默认为获取图片
                            while (data.moveToNext()) {
                                String path = data.getString(data.getColumnIndex(PROJECTION[1]));
                                String mimeType = data.getString(data.getColumnIndex(PROJECTION[2]));
                                if (isAndroidQ) { //适配 android Q
                                    long id = data.getLong(data.getColumnIndex(PROJECTION[0]));
                                    path = AndroidQTransformUtils.parseImagePathToAndroidQ(context, getRealPathAndroid_Q(id), mimeType);
                                }
                                long time = data.getLong(data.getColumnIndex(PROJECTION[3]));
                                String name = data.getString(data.getColumnIndex(PROJECTION[4]));
                                //过滤未下载完成或者不存在的文件
                                if (!TextUtils.equals(MediaUtil.getExtensionName(path), "downloading") && MediaUtil.isFileExists(path)) {
                                    images.add(new MediaBean(path, time, name, mimeType));
                                }
                            }
                        }
                        data.close();
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_QUERY_MEDIA_SUCCESS, splitFolder(context, images)));
                } catch (Exception e) {
                    mHandler.sendEmptyMessage(MSG_QUERY_MEDIA_ERROR);
                }
            }
        });
    }

    private String getSelection() {
        switch (mChooseMode) {
            case 1:
                return SELECTION_NOT_GIF;
            case 2:
                return getSelectionArgsForSingleMediaCondition();
        }
        return null;
    }

    private String[] getSelectionArgs() {
        switch (mChooseMode) {
            case 1:
                // 只获取图片
                return getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            case 2:
                // 只获取视频
                return getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        }
        return null;
    }

    /**
     * 适配Android Q
     */
    private String getRealPathAndroid_Q(long id) {
        return QUERY_URI.buildUpon().appendPath(MediaUtil.toString(id)).build().toString();
//        if (!TextUtils.isEmpty(path) && path.startsWith(MediaConfig.CONTENT_PATH)) {
//            return MediaUtil.getPath(context.getApplicationContext(), Uri.parse(path));
//        }
//        return path;
    }

    /**
     * 把图片按文件夹拆分，第一个文件夹保存所有的图片
     */
    private ArrayList<MediaFolder> splitFolder(Context context, ArrayList<MediaBean> images) {
        ArrayList<MediaFolder> folders = new ArrayList<>();
        folders.add(new MediaFolder(context.getString(mChooseMode == 2 ? R.string.imagePicker_allVideos :
                R.string.imagePicker_allPictures), images));
        if (images != null && !images.isEmpty()) {
            int size = images.size();
            for (int i = 0; i < size; i++) {
                String path = images.get(i).getPath();
                String folderName = getFolderName(path);
                if (StringUtils.isNotEmptyString(folderName)) {
                    MediaFolder folder = getFolder(folderName, folders);
                    folder.addImage(images.get(i));
                }
            }
        }
        return folders;
    }

    /**
     * 根据图片路径，获取图片文件夹名称
     */
    private static String getFolderName(String path) {
        if (StringUtils.isNotEmptyString(path)) {
            String[] strings = path.split(File.separator);
            if (strings.length >= 2) {
                return strings[strings.length - 2];
            }
        }
        return "";
    }

    private static MediaFolder getFolder(String name, List<MediaFolder> folders) {
        if (!folders.isEmpty()) {
            int size = folders.size();
            for (int i = 0; i < size; i++) {
                MediaFolder folder = folders.get(i);
                if (TextUtils.equals(folder.getName(), name)) {
                    return folder;
                }
            }
        }
        MediaFolder newFolder = new MediaFolder(name);
        folders.add(newFolder);
        return newFolder;
    }

    /**
     * 获取指定类型的文件
     */
    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[]{String.valueOf(mediaType)};
    }

    /**
     * 查询(视频)
     */
    private static String getSelectionArgsForSingleMediaCondition() {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (mCompleteListener == null) return false;
        switch (msg.what) {
            case MSG_QUERY_MEDIA_SUCCESS:
                mCompleteListener.loadComplete((ArrayList<MediaFolder>) msg.obj);
                break;
            case MSG_QUERY_MEDIA_ERROR:
                mCompleteListener.loadMediaDataError();
                break;
        }
        return false;
    }

    public void release() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public interface OnCompleteListener {
        void onPreLoad();

        void loadComplete(ArrayList<MediaFolder> folders);

        void loadMediaDataError();
    }
}
