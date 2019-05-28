package com.gallopmark.imagepicker.view;


import com.gallopmark.imagepicker.bean.ImageFolder;

import java.util.ArrayList;

public interface ImagePickerView extends BaseView{

    void onInitImageList();

    void onCheckExternalPermission();

    void onLoadFolders(ArrayList<ImageFolder> folders);

}
