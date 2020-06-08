package pony.xcode.media;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import pony.xcode.media.adapter.MediaFolderAdapter;
import pony.xcode.media.adapter.MediaGridAdapter;
import pony.xcode.media.bean.MediaFolder;
import pony.xcode.media.bean.MediaBean;
import pony.xcode.media.presenter.MediaPickerPresenter;
import pony.xcode.media.view.MediaPickerView;

import java.util.ArrayList;
import java.util.List;

/*图片选择页面*/
public class MediaPickerActivity extends MediaBaseActivity implements View.OnClickListener, MediaPickerView {

    private Toolbar mToolbar;
    private FrameLayout mConfirmLayout;
    private TextView mConfirmWidget;
    private RecyclerView mMediaRView;
    private TextView mTimeTextView;
    private FrameLayout mFolderLayout;
    private RecyclerView mFolderRView;
    private FrameLayout mFolderNameLayout;
    private TextView mFolderNameTView;
    private FrameLayout mPreviewLayout;
    private TextView mPreviewTView;

    private boolean mSingleChoice;
    private boolean isViewImage = true;
    private int mMaxCount;

    private boolean mUseCamera = true;

    //用于接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开选择器，允许用
    // 户把先前选过的图片传进来，并把这些图片默认为选中状态。
    private static List<MediaBean> mTempSelectItems;
    private List<MediaBean> mSelectedItems;

    private MediaFolder mMediaFolder;
    private List<MediaFolder> mFolderList;

    private MediaGridAdapter mGridAdapter;

    private MediaPickerPresenter mPresenter;

    /*用于接收图片预览页面已选图片集合，不用intent传值（避免数据过大发生错误），在onActivityResult回调时刷新当前
    已选图片数据，在activity结束时赋值为null，避免造成内存泄漏*/
    private static List<MediaBean> tempSelectItems;

    private int mChooseMode = MediaConfig.MODE_IMAGE;

    /**
     * 启动图片选择器
     *
     * @param activity       activity
     * @param requestCode    requestCode
     * @param isSingle       是否单选
     * @param isViewImage    是否点击放大图片查看
     * @param useCamera      是否使用拍照功能
     * @param maxSelectCount 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     * @param selected       接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
     *                       选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
     */
    public static void openActivity(Activity activity, int requestCode,
                                    boolean isSingle, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount, ArrayList<MediaBean> selected,
                                    int durationLimit, int videoQuality,
                                    long sizeLimit, int mode) {
        Intent intent = new Intent(activity, MediaPickerActivity.class);
        intent.putExtras(dataPackages(isSingle, isViewImage,
                useCamera, maxSelectCount,
                selected, durationLimit,
                videoQuality, sizeLimit,
                mode));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动图片选择器
     *
     * @param fragment       fragment
     * @param requestCode    requestCode
     * @param isSingle       是否单选
     * @param isViewImage    是否点击放大图片查看
     * @param useCamera      是否使用拍照功能
     * @param maxSelectCount 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     * @param selected       接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
     *                       选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
     */
    public static void openActivity(Fragment fragment, int requestCode,
                                    boolean isSingle, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount, ArrayList<MediaBean> selected,
                                    int durationLimit, int videoQuality,
                                    long sizeLimit, int mode) {
        if (fragment.getContext() == null) return;
        Intent intent = new Intent(fragment.getContext(), MediaPickerActivity.class);
        intent.putExtras(dataPackages(isSingle, isViewImage,
                useCamera, maxSelectCount,
                selected, durationLimit,
                videoQuality, sizeLimit,
                mode));
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void openActivity(android.app.Fragment fragment, int requestCode,
                                    boolean isSingle, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount, ArrayList<MediaBean> selected,
                                    int durationLimit, int videoQuality,
                                    long sizeLimit, int mode) {
        if (fragment.getActivity() == null) return;
        Intent intent = new Intent(fragment.getActivity(), MediaPickerActivity.class);
        intent.putExtras(dataPackages(isSingle, isViewImage,
                useCamera, maxSelectCount,
                selected, durationLimit,
                videoQuality, sizeLimit,
                mode));
        fragment.startActivityForResult(intent, requestCode);
    }

    private static Bundle dataPackages(boolean isSingle, boolean isViewImage,
                                       boolean useCamera, int maxSelectCount,
                                       ArrayList<MediaBean> selected, int durationLimit,
                                       int videoQuality, long sizeLimit,
                                       int mode) {
        Bundle bundle = new Bundle();
        bundle.putInt(MediaPicker.CHOOSE_MODE, mode);
        bundle.putBoolean(MediaPicker.IS_SINGLE, isSingle);
        bundle.putBoolean(MediaPicker.IS_VIEW_IMAGE, isViewImage);
        bundle.putBoolean(MediaPicker.USE_CAMERA, useCamera);
        bundle.putInt(MediaPicker.MAX_SELECT_COUNT, maxSelectCount);
        bundle.putInt(MediaPicker.DURATION_LIMIT, durationLimit);
        bundle.putInt(MediaPicker.VIDEO_QUALITY, videoQuality);
        bundle.putLong(MediaPicker.SIZE_LIMIT, sizeLimit);
        mTempSelectItems = selected;
        return bundle;
    }

    public static void onPreviewResult(Activity activity, List<MediaBean> selectItems, boolean isConfirm) {
        tempSelectItems = selectItems;
        Intent intent = new Intent();
        intent.putExtra(MediaPicker.IS_CONFIRM, isConfirm);
        activity.setResult(MediaPicker.PREVIEW_RESULT_CODE, intent);
    }

    @Override
    protected int bindContentView() {
        return R.layout.mediaselector_activity_media_picker;
    }

    @Override
    protected void setup(@Nullable Bundle savedInstanceState) {
        mPresenter = new MediaPickerPresenter(this, this);
        mPresenter.initView();
        setSelectImageCount(0);
    }

    @Override
    public void onGetIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mChooseMode = bundle.getInt(MediaPicker.CHOOSE_MODE, MediaConfig.MODE_IMAGE);
            mMaxCount = bundle.getInt(MediaPicker.MAX_SELECT_COUNT, 0);
            mSingleChoice = bundle.getBoolean(MediaPicker.IS_SINGLE, false);
            isViewImage = bundle.getBoolean(MediaPicker.IS_VIEW_IMAGE, true);
            mUseCamera = bundle.getBoolean(MediaPicker.USE_CAMERA, true);
            if (mTempSelectItems != null) {
                this.mSelectedItems = new ArrayList<>(mTempSelectItems);
                mTempSelectItems.clear();
                mTempSelectItems = null;
            }
        }
    }

    @Override
    public void onInitView() {
        mToolbar = findViewById(R.id.toolBar);
        if (mChooseMode == MediaConfig.MODE_VIDEO) {
            mToolbar.setTitle(getString(R.string.imagePicker_video_title));
        } else {
            mToolbar.setTitle(getString(R.string.imagePicker_image_title));
        }
        mConfirmLayout = findViewById(R.id.mConfirmLayout);
        mConfirmWidget = findViewById(R.id.mConfirmTv);
        mMediaRView = findViewById(R.id.mImageRv);
        mTimeTextView = findViewById(R.id.mTimeTextView);
        mFolderLayout = findViewById(R.id.mFolderLayout);
        mFolderRView = findViewById(R.id.mFolderRv);
        mFolderNameLayout = findViewById(R.id.mFolderNameLayout);
        mFolderNameTView = findViewById(R.id.mFolderNameTv);
        mPreviewLayout = findViewById(R.id.mPreviewLayout);
        mPreviewTView = findViewById(R.id.mPreviewTv);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mMediaRView.setLayoutManager(gridLayoutManager);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mFolderRView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onInitListener() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mConfirmLayout.setOnClickListener(this);
        mFolderLayout.setOnClickListener(this);
        mFolderNameLayout.setOnClickListener(this);
        mPreviewLayout.setOnClickListener(this);
        mMediaRView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                changeTime();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                changeTime();
            }
        });
    }

    @Override
    public void onInitImageList() {
        mGridAdapter = new MediaGridAdapter(this, mMaxCount, mSingleChoice, isViewImage, mChooseMode);
        mMediaRView.setAdapter(mGridAdapter);
        mGridAdapter.setOnItemSelectListener(new MediaGridAdapter.OnItemSelectListener() {
            @Override
            public void onSelected(List<MediaBean> selectImages) {
                setSelectImageCount(selectImages.size());
            }
        });
        mGridAdapter.setOnItemClickListener(new MediaGridAdapter.OnItemClickListener() {
            @Override
            public void onCameraClick() {
                mPresenter.checkPermissionAndCamera();
            }

            @Override
            public void onItemClick(MediaBean mediaBean, int position) {
                toPreviewActivity(mGridAdapter.getData(), position);
            }
        });
    }

    @Override
    public void onCheckExternalPermission() {
        mPresenter.checkPermissionAndLoadImages(mChooseMode);
    }

    @Override
    public void onLoadFolders(ArrayList<MediaFolder> folders) {
        this.mFolderList = folders;
        initFolderList();
        if (this.mFolderList.isEmpty()) return;  /*空相册*/
        mFolderList.get(0).setUseCamera(mUseCamera);
        setFolder(mFolderList.get(0));
        if (mSelectedItems != null && mGridAdapter != null) {
            mGridAdapter.setSelectedImages(mSelectedItems);
            setSelectImageCount(mGridAdapter.getSelectedItems().size());
        }
    }

    /**
     * 改变时间条显示的时间（显示图片列表中的第一个可见图片的时间）
     */
    private void changeTime() {
        if (mMediaRView.getLayoutManager() == null) return;
        int firstVisibleItem = ((GridLayoutManager) mMediaRView.getLayoutManager()).findFirstVisibleItemPosition();
        MediaBean mediaBean = mGridAdapter.getFirstVisibleImage(firstVisibleItem);
        mPresenter.changeTime(mTimeTextView, mediaBean);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.mConfirmLayout) {
            confirm();
        } else if (viewId == R.id.mFolderNameLayout) {
            if (mPresenter.isLoadImage()) {
                if (mFolderLayout.getVisibility() != View.VISIBLE) {
                    mPresenter.openFolder(mFolderLayout, mFolderNameLayout);
                } else {
                    mPresenter.closeFolder(mFolderLayout, mFolderNameLayout);
                }
            }
        } else if (viewId == R.id.mFolderLayout) {
            mPresenter.closeFolder(mFolderLayout, mFolderNameLayout);
        } else if (viewId == R.id.mPreviewLayout) {
            toPreviewActivity(mGridAdapter.getSelectedItems(), 0);
        }
    }

    private void initFolderList() {
        MediaFolderAdapter folderAdapter = new MediaFolderAdapter(this, mFolderList);
        mFolderRView.setAdapter(folderAdapter);
        folderAdapter.setOnFolderSelectListener(new MediaFolderAdapter.OnFolderSelectListener() {
            @Override
            public void OnFolderSelect(MediaFolder folder) {
                setFolder(folder);
            }
        });
    }

    /**
     * 设置选中的文件夹，同时刷新图片列表
     */
    private void setFolder(MediaFolder folder) {
        if (folder != null && mGridAdapter != null && !folder.equals(mMediaFolder)) {
            mMediaFolder = folder;
            mFolderNameTView.setText(folder.getName());
            mMediaRView.scrollToPosition(0);
            mGridAdapter.refresh(folder.getItemList(), folder.isUseCamera());
            mPresenter.closeFolder(mFolderLayout, mFolderNameLayout);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MediaPicker.PREVIEW_RESULT_CODE) {
            if (tempSelectItems != null) {
                mGridAdapter.setSelectedImages(tempSelectItems);
                setSelectImageCount(mGridAdapter.getSelectedItems().size());
                tempSelectItems = null;
            }
            if (data != null && data.getBooleanExtra(MediaPicker.IS_CONFIRM, false)) {
                //如果用户在预览页点击了确定，就直接把用户选中的图片返回给用户。
                confirm();
            }
        } else {
            mPresenter.onActivityResult(requestCode, resultCode);
        }
    }

    private void confirm() {
        if (mGridAdapter == null) {
            return;
        }
        //点击确定，把选中的图片通过Intent传给上一个Activity。
        mPresenter.setResult(RESULT_OK, mGridAdapter.getSelectedItems(), false);
        finish();
    }

    private void setSelectImageCount(int count) {
        String textConfirm = getString(R.string.imagePicker_confirm);
        String textPreview = getString(R.string.imagePicker_preview);
        if (count == 0) {
            mConfirmLayout.setEnabled(false);
            mPreviewLayout.setEnabled(false);
        } else {
            mConfirmLayout.setEnabled(true);
            mPreviewLayout.setEnabled(true);
            textPreview = textPreview + "(" + count + ")";
            if (mSingleChoice) {
                textConfirm = getString(R.string.imagePicker_confirm);
            } else if (mMaxCount > 0) {
                textConfirm = textConfirm + "(" + count + "/" + mMaxCount + ")";
            } else {
                textConfirm = textConfirm + "(" + count + ")";
            }
        }
        mConfirmWidget.setText(textConfirm);
        mPreviewTView.setText(textPreview);
    }

    private void toPreviewActivity(List<MediaBean> images, int position) {
        if (images != null && !images.isEmpty()) {
            MediaPreviewActivity.openActivity(this, mChooseMode, images, mGridAdapter.getSelectedItems(), mSingleChoice, mMaxCount, position);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mPresenter.onReStart();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && mFolderLayout.getVisibility() != View.GONE) {
            mPresenter.closeFolder(mFolderLayout, mFolderNameLayout);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (tempSelectItems != null && !tempSelectItems.isEmpty()) {
            tempSelectItems.clear();
        }
        tempSelectItems = null;
        mPresenter.destroy();
        super.onDestroy();
    }
}
