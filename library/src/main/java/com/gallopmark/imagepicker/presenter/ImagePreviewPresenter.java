package com.gallopmark.imagepicker.presenter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.gallopmark.imagepicker.utils.AnimationUtil;
import com.gallopmark.imagepicker.view.ImagePreviewView;

public class ImagePreviewPresenter {

    private ImagePreviewView previewView;

    private ImagePreviewPresenter(ImagePreviewView previewView) {
        this.previewView = previewView;
    }

    public static ImagePreviewPresenter from(@NonNull ImagePreviewView previewView) {
        return new ImagePreviewPresenter(previewView);
    }

    public void initView() {
        previewView.onGetIntent();
        previewView.onInitView();
        previewView.initViewData();
        previewView.initViewPager();
        previewView.onInitListener();
    }

    public void showBar(FrameLayout mContainer, Toolbar toolbar, LinearLayout mBottom) {
        AnimationUtil.topMoveToViewLocation(toolbar, 200);
        AnimationUtil.bottomMoveToViewLocation(mBottom, 200);
        mContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    public void hideBar(FrameLayout mContainer, Toolbar toolbar, LinearLayout mBottom) {
        AnimationUtil.moveToViewTop(toolbar, 200);
        AnimationUtil.moveToViewBottom(mBottom, 200);
        mContainer.setSystemUiVisibility(View.INVISIBLE);
    }
}
