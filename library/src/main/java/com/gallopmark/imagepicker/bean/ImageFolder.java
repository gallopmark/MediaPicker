package com.gallopmark.imagepicker.bean;

import androidx.annotation.NonNull;

import com.gallopmark.imagepicker.utils.StringUtils;

import java.util.ArrayList;

/**
 * 图片文件夹实体类
 */
public class ImageFolder {
    private boolean useCamera; // 是否可以调用相机拍照。只有“全部”文件夹才可以拍照
    private String name;
    private ArrayList<ImageItem> images;

    public ImageFolder(String name) {
        this.name = name;
    }

    public ImageFolder(String name, ArrayList<ImageItem> images) {
        this.name = name;
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ImageItem> getImages() {
        return images;
    }

    public boolean isUseCamera() {
        return useCamera;
    }

    public void setUseCamera(boolean useCamera) {
        this.useCamera = useCamera;
    }

    public void addImage(ImageItem image) {
        if (image != null && StringUtils.isNotEmptyString(image.getPath())) {
            if (images == null) {
                images = new ArrayList<>();
            }
            images.add(image);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Folder{" +
                "name='" + name + '\'' +
                ", images=" + images +
                '}';
    }
}
