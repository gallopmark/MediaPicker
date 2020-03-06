package pony.xcode.media.presenter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import pony.xcode.media.utils.AnimationUtil;
import pony.xcode.media.view.MediaPreviewView;

public class MediaPreviewPresenter {

    private MediaPreviewView previewView;

    private MediaPreviewPresenter(MediaPreviewView previewView) {
        this.previewView = previewView;
    }

    public static MediaPreviewPresenter from(@NonNull MediaPreviewView previewView) {
        return new MediaPreviewPresenter(previewView);
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
