package pony.xcode.media.view;


import pony.xcode.media.bean.MediaFolder;

import java.util.ArrayList;

public interface MediaPickerView extends BaseView{

    void onInitImageList();

    void onCheckExternalPermission();

    void onLoadFolders(ArrayList<MediaFolder> folders);

}
