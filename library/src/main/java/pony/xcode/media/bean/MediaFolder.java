package pony.xcode.media.bean;

import androidx.annotation.NonNull;

import pony.xcode.media.utils.StringUtils;

import java.util.ArrayList;

/**
 * 图片文件夹实体类
 */
public class MediaFolder {
    private boolean useCamera; // 是否可以调用相机拍照。只有“全部”文件夹才可以拍照
    private String name;
    private ArrayList<MediaBean> itemList;

    public MediaFolder(String name) {
        this.name = name;
    }

    public MediaFolder(String name, ArrayList<MediaBean> images) {
        this.name = name;
        this.itemList = images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<MediaBean> getItemList() {
        return itemList;
    }

    public boolean isUseCamera() {
        return useCamera;
    }

    public void setUseCamera(boolean useCamera) {
        this.useCamera = useCamera;
    }

    public void addImage(MediaBean image) {
        if (image != null && StringUtils.isNotEmptyString(image.getPath())) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(image);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Folder{" +
                "name='" + name + '\'' +
                ", images=" + itemList +
                '}';
    }
}
