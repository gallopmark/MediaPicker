package com.gallopmark.imagepicker.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.gallopmark.imagepicker.R;
import com.gallopmark.imagepicker.bean.ImageFolder;
import com.gallopmark.imagepicker.bean.ImageItem;
import com.gallopmark.imagepicker.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageSource {
    /**
     * 从SDCard加载图片
     */
    public static void loadImageForSDCard(final Context context, final OnGetImageCallback callback) {
        //由于扫描图片是耗时的操作，所以要在子线程处理。
        new Thread(new Runnable() {
            @Override
            public void run() {
                //扫描图片
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = context.getContentResolver();
                Cursor mCursor = mContentResolver.query(mImageUri, new String[]{
                                MediaStore.Images.Media.DATA,
                                MediaStore.Images.Media.DISPLAY_NAME,
                                MediaStore.Images.Media.DATE_ADDED,
                                MediaStore.Images.Media._ID,
                                MediaStore.Images.Media.MIME_TYPE},
                        null,
                        null,
                        MediaStore.Images.Media.DATE_ADDED);
                ArrayList<ImageItem> images = new ArrayList<>();
                //读取扫描到的图片
                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        //获取图片名称
                        String name = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        //获取图片时间
                        long time = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                        //获取图片类型
                        String mimeType = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                        //过滤未下载完成或者不存在的文件
                        if (!TextUtils.equals(getExtensionName(path), "downloading") && checkImgExists(path)) {
                            images.add(new ImageItem(path, time, name, mimeType));
                        }
                    }
                    mCursor.close();
                }
                Collections.reverse(images);
                callback.onSuccess(splitFolder(context, images));
            }
        }).start();
    }

    /**
     * 检查图片是否存在。ContentResolver查询处理的数据有可能文件路径并不存在。
     */
    private static boolean checkImgExists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * 把图片按文件夹拆分，第一个文件夹保存所有的图片
     */
    private static ArrayList<ImageFolder> splitFolder(Context context, ArrayList<ImageItem> images) {
        ArrayList<ImageFolder> folders = new ArrayList<>();
        folders.add(new ImageFolder(context.getString(R.string.imagePicker_allPictures), images));
        if (images != null && !images.isEmpty()) {
            int size = images.size();
            for (int i = 0; i < size; i++) {
                String path = images.get(i).getPath();
                String folderName = getFolderName(path);
                if (StringUtils.isNotEmptyString(folderName)) {
                    ImageFolder folder = getFolder(folderName, folders);
                    folder.addImage(images.get(i));
                }
            }
        }
        return folders;
    }

    /**
     * Java文件操作 获取文件扩展名
     */
    private static String getExtensionName(String filename) {
        if (filename != null && filename.length() > 0) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1 && dot < filename.length() - 1) {
                return filename.substring(dot + 1);
            }
        }
        return "";
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

    private static ImageFolder getFolder(String name, List<ImageFolder> folders) {
        if (!folders.isEmpty()) {
            int size = folders.size();
            for (int i = 0; i < size; i++) {
                ImageFolder folder = folders.get(i);
                if(TextUtils.equals(folder.getName(),name)){
                    return folder;
                }
            }
        }
        ImageFolder newFolder = new ImageFolder(name);
        folders.add(newFolder);
        return newFolder;
    }

    public interface OnGetImageCallback {
        void onSuccess(ArrayList<ImageFolder> folders);
    }
}
