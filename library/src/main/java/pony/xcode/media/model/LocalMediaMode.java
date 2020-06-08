package pony.xcode.media.model;

import android.annotation.SuppressLint;
import android.content.ContentUris;
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
import pony.xcode.media.utils.MediaUtil;
import pony.xcode.media.utils.SDKVersionUtils;
import pony.xcode.media.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalMediaMode implements Handler.Callback {

    private static final int MSG_QUERY_MEDIA_SUCCESS = 0;
    private static final int MSG_QUERY_MEDIA_ERROR = -1;

    private static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    private static final String NOT_GIF = "!='image/gif'";  //过滤掉gif图
    private static final String IMAGE_SELECTION = MediaStore.Images.ImageColumns.SIZE + ">0"
            + " AND " + MediaStore.Images.ImageColumns.MIME_TYPE + NOT_GIF;
    private static final String IMAGE_SORT_ORDER = MediaStore.Images.ImageColumns._ID + " DESC";

    @SuppressLint("InlinedApi")
    private static final String VIDEO_SELECTION = MediaStore.Video.VideoColumns.SIZE + ">0"
            + " AND " + MediaStore.Video.VideoColumns.DURATION + ">0";
    private static final String VIDEO_SORT_ORDER = MediaStore.Video.VideoColumns._ID + " DESC";

    private int mChooseMode;
    private Handler mHandler;
    private OnCompleteListener mCompleteListener;

    /**
     * 从SDCard加载图片或视频
     */
    public void loadMediaForSDCard(final Context context, final int mode, final OnCompleteListener listener) {
        //由于扫描图片是耗时的操作，所以要在子线程处理。
        mChooseMode = mode;
        mHandler = new Handler(Looper.getMainLooper(), this);
        mCompleteListener = listener;
        mCompleteListener.onPreLoad();
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {

            @Override
            public void run() {
                Cursor cursor = null;
                try {
                    ArrayList<MediaBean> beanList = new ArrayList<>();
                    final Uri queryUri;
                    final String[] projection;
                    final String selection;
                    final String sortOrder;
                    if (mChooseMode == MediaConfig.MODE_VIDEO) {
                        queryUri = VIDEO_URI;
                        projection = getVideoProjection();
                        selection = VIDEO_SELECTION;
                        sortOrder = VIDEO_SORT_ORDER;
                    } else {
                        queryUri = IMAGE_URI;
                        projection = getImageProjection();
                        selection = IMAGE_SELECTION;
                        sortOrder = IMAGE_SORT_ORDER;
                    }
                    cursor = context.getContentResolver().query(queryUri, projection, selection, null, sortOrder);
                    if (cursor != null) {
                        if (mChooseMode == MediaConfig.MODE_VIDEO) { //获取视频
                            while (cursor.moveToNext()) {
                                long duration = cursor.getLong(cursor.getColumnIndex(projection[5]));
                                long size = cursor.getLong(cursor.getColumnIndex(projection[6]));
                                if (duration == 0 || size <= 0) {
                                    // 时长如果为0，就当做损坏的视频处理过滤掉、视频大小为0过滤掉
                                    continue;
                                }
                                long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                                Uri uri = ContentUris.withAppendedId(queryUri, id);
                                //android Q 以下为真实路径
                                String path = cursor.getString(cursor.getColumnIndex(projection[1]));
                                if (SDKVersionUtils.isAndroidQAbove()) {
                                    path = MediaUtil.getPath(context, uri);
                                }
                                if (!MediaUtil.isFileExists(path)) {
                                    continue;
                                }
                                String mimeType = cursor.getString(cursor.getColumnIndex(projection[2]));
                                long time = cursor.getLong(cursor.getColumnIndex(projection[3]));
                                String name = cursor.getString(cursor.getColumnIndex(projection[4]));
                                beanList.add(new MediaBean(path, time, name, mimeType, duration, size, uri));
                            }
                        } else {  //默认为获取图片
                            while (cursor.moveToNext()) {
                                long size = cursor.getLong(cursor.getColumnIndex(projection[5]));
                                if (size == 0) {
                                    continue;
                                }
                                long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                                //获取图片uri
                                Uri uri = ContentUris.withAppendedId(queryUri, id);
                                String path = cursor.getString(cursor.getColumnIndex(projection[1]));
                                if (SDKVersionUtils.isAndroidQAbove()) {
                                    path = MediaUtil.getPath(context, uri);
                                }
                                if (!MediaUtil.isFileExists(path)) {
                                    continue;
                                }
                                String mimeType = cursor.getString(cursor.getColumnIndex(projection[2]));
                                long time = cursor.getLong(cursor.getColumnIndex(projection[3]));
                                String name = cursor.getString(cursor.getColumnIndex(projection[4]));
                                beanList.add(new MediaBean(path, time, name, mimeType, size, uri));
                            }
                        }
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_QUERY_MEDIA_SUCCESS, splitFolder(context, beanList)));
                } catch (Exception e) {
                    mHandler.sendEmptyMessage(MSG_QUERY_MEDIA_ERROR);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    @SuppressLint("InlinedApi")
    private String[] getVideoProjection() {
        return new String[]{
                MediaStore.Video.VideoColumns._ID, //id
                MediaStore.Video.VideoColumns.DATA,   //路径
                MediaStore.Video.VideoColumns.MIME_TYPE,  //mime type
                MediaStore.Video.VideoColumns.DATE_ADDED, //添加的时间
                MediaStore.Video.VideoColumns.DISPLAY_NAME, //名称
                MediaStore.Video.VideoColumns.DURATION,   //时长
                MediaStore.Video.VideoColumns.SIZE,    //大小
        };
    }

    private String[] getImageProjection() {
        return new String[]{
                MediaStore.Images.ImageColumns._ID, //id
                MediaStore.Images.ImageColumns.DATA,   //路径
                MediaStore.Images.ImageColumns.MIME_TYPE,  //mime type
                MediaStore.Images.ImageColumns.DATE_ADDED, //添加的时间
                MediaStore.Images.ImageColumns.DISPLAY_NAME, //名称
                MediaStore.Images.ImageColumns.SIZE,    //大小
        };
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
            mHandler.removeMessages(MSG_QUERY_MEDIA_SUCCESS);
            mHandler.removeMessages(MSG_QUERY_MEDIA_ERROR);
            mHandler = null;
        }
    }

    public interface OnCompleteListener {
        void onPreLoad();

        void loadComplete(ArrayList<MediaFolder> folders);

        void loadMediaDataError();
    }
}
