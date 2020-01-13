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
import com.gallopmark.imagepicker.loader.DefaultImageLoader;
import com.gallopmark.imagepicker.loader.ImageLoader;
import com.gallopmark.imagepicker.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageSource {

    private volatile static ImageSource mInstance;
    private ImageLoader mImageLoader;

    private ImageSource() {

    }

    public static ImageSource getInstance() {
        if (mInstance == null) {
            synchronized (ImageSource.class) {
                if (mInstance == null) {
                    mInstance = new ImageSource();
                }
            }
        }
        return mInstance;
    }

    /*设置图片加载器*/
    void setDisplacer(ImageLoader imageLoader) {
        this.mImageLoader = imageLoader;
    }

    /*如果未设置图片加载器，则使用默认图片加载器（项目中需要引用glide图片加载库）*/
    private ImageLoader getImageLoader() {
        return mImageLoader == null ? new DefaultImageLoader() : mImageLoader;
    }

    public static ImageLoader getDisplacer() {
        return getInstance().getImageLoader();
    }

    /**
     * 从SDCard加载图片
     */
    public void loadImageForSDCard(final Context context, final OnGetImageCallback callback) {
        //由于扫描图片是耗时的操作，所以要在子线程处理。
        new Thread(new Runnable() {
            @Override
            public void run() {
                //扫描图片
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = context.getContentResolver();
                final String[] projection = new String[]{
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media.MIME_TYPE};
                Cursor cursor = contentResolver.query(imageUri, projection, null, null, MediaStore.Images.Media.DATE_ADDED);
                ArrayList<ImageItem> images = new ArrayList<>();
                //读取扫描到的图片
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        //ID 是在 Android Q 上读取文件的关键字段
//                        int id = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                        //注意，DATA 数据在 Android Q 以前代表了文件的路径，但在 Android Q上该路径无法被访问，因此没有意义。
//                        String path;
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                            path = getRealPath(id);
//                        } else {
//                            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//                        }
                        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        //获取图片名称
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        //获取图片时间
                        long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                        //获取图片类型
                        String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                        //过滤未下载完成或者不存在的文件
                        if (!TextUtils.equals(getExtensionName(path), "downloading") && checkImgExists(path)) {
                            images.add(new ImageItem(path, time, name, mimeType));
                        }
                    }
                    cursor.close();
                }
                Collections.reverse(images);
                callback.onSuccess(splitFolder(context, images));
            }
        }).start();
    }

    //既然 data 不可用，就需要知晓 id 的使用方式，首先是使用 id 拼装出 content uri
//    private String getRealPath(int id) {
//        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
//                .appendPath(String.valueOf(id)).build().toString();
//    }

    /**
     * Java文件操作 获取文件扩展名
     */
    private String getExtensionName(String filename) {
        if (filename != null && filename.length() > 0) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1 && dot < filename.length() - 1) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    /**
     * 检查图片是否存在。ContentResolver查询处理的数据有可能文件路径并不存在。
     */
    private boolean checkImgExists(String filePath) {
        if (TextUtils.isEmpty(filePath)) return false;
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
                if (TextUtils.equals(folder.getName(), name)) {
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
