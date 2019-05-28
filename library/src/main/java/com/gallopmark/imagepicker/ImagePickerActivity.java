package com.gallopmark.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gallopmark.imagepicker.adapter.ImageFolderAdapter;
import com.gallopmark.imagepicker.adapter.ImageGridAdapter;
import com.gallopmark.imagepicker.bean.ImageFolder;
import com.gallopmark.imagepicker.bean.ImageItem;
import com.gallopmark.imagepicker.presenter.ImagePickerPresenter;
import com.gallopmark.imagepicker.model.ImagePicker;
import com.gallopmark.imagepicker.view.ImagePickerView;

import java.util.ArrayList;
import java.util.List;


public class ImagePickerActivity extends ImageActivity implements View.OnClickListener, ImagePickerView {

    private Toolbar toolBar;
    private FrameLayout mConfirmLayout;
    private TextView mConfirmTv;
    private RecyclerView mImageRv;
    private TextView mTimeTextView;
    private FrameLayout mFolderLayout;
    private RecyclerView mFolderRv;
    private FrameLayout mFolderNameLayout;
    private TextView mFolderNameTv;
    private FrameLayout mPreviewLayout;
    private TextView mPreviewTv;

    private boolean isSingle;
    private boolean isViewImage = true;
    private int mMaxCount;

    private boolean useCamera = true;

    //用于接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开选择器，允许用
    // 户把先前选过的图片传进来，并把这些图片默认为选中状态。
    private static List<String> mTempSelectImages;
    private List<ImageItem> mSelectedImages;

    private ImageFolder mFolder;
    private List<ImageFolder> mFolders;

    private ImageGridAdapter mGridAdapter;

    private ImagePickerPresenter mPresenter;

    /*用于接收图片预览页面已选图片集合，不用intent传值（避免数据过大发生错误），在onActivityResult回调时刷新当前
    已选图片数据，在activity结束时赋值为null，避免造成内存泄漏*/
    private static List<ImageItem> tempSelectImages;

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
                                    int maxSelectCount, ArrayList<String> selected) {
        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtras(dataPackages(isSingle, isViewImage, useCamera, maxSelectCount, selected));
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
                                    int maxSelectCount, ArrayList<String> selected) {
        if (fragment.getContext() == null) return;
        Intent intent = new Intent(fragment.getContext(), ImagePickerActivity.class);
        intent.putExtras(dataPackages(isSingle, isViewImage, useCamera, maxSelectCount, selected));
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void openActivity(android.app.Fragment fragment, int requestCode,
                                    boolean isSingle, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount, ArrayList<String> selected) {
        if (fragment.getActivity() == null) return;
        Intent intent = new Intent(fragment.getActivity(), ImagePickerActivity.class);
        intent.putExtras(dataPackages(isSingle, isViewImage, useCamera, maxSelectCount, selected));
        fragment.startActivityForResult(intent, requestCode);
    }

    private static Bundle dataPackages(boolean isSingle, boolean isViewImage, boolean useCamera,
                                       int maxSelectCount, ArrayList<String> selected) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ImagePicker.IS_SINGLE, isSingle);
        bundle.putBoolean(ImagePicker.IS_VIEW_IMAGE, isViewImage);
        bundle.putBoolean(ImagePicker.USE_CAMERA, useCamera);
        bundle.putInt(ImagePicker.MAX_SELECT_COUNT, maxSelectCount);
        mTempSelectImages = selected;
        return bundle;
    }

    public static void onPreviewResult(Activity activity, List<ImageItem> mSelectItems, boolean isConfirm) {
        tempSelectImages = mSelectItems;
        Intent intent = new Intent();
        intent.putExtra(ImagePicker.IS_CONFIRM, isConfirm);
        activity.setResult(ImagePicker.PREVIEW_RESULT_CODE, intent);
    }

    @Override
    protected int bindContentView() {
        return R.layout.activity_imagepicker;
    }

    @Override
    protected void setup(@Nullable Bundle savedInstanceState) {
        mPresenter = new ImagePickerPresenter(this, this);
        mPresenter.initView();
        setSelectImageCount(0);
    }

    @Override
    public void onGetIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mMaxCount = bundle.getInt(ImagePicker.MAX_SELECT_COUNT, 0);
            isSingle = bundle.getBoolean(ImagePicker.IS_SINGLE, false);
            isViewImage = bundle.getBoolean(ImagePicker.IS_VIEW_IMAGE, true);
            useCamera = bundle.getBoolean(ImagePicker.USE_CAMERA, true);
            if (mTempSelectImages != null) {
                this.mSelectedImages = new ArrayList<>();
                for (String path : mTempSelectImages) {
                    this.mSelectedImages.add(new ImageItem(path));
                }
                mTempSelectImages.clear();
                mTempSelectImages = null;
            }
        }
    }

    @Override
    public void onInitView() {
        toolBar = findViewById(R.id.toolBar);
        mConfirmLayout = findViewById(R.id.mConfirmLayout);
        mConfirmTv = findViewById(R.id.mConfirmTv);
        mImageRv = findViewById(R.id.mImageRv);
        mTimeTextView = findViewById(R.id.mTimeTextView);
        mFolderLayout = findViewById(R.id.mFolderLayout);
        mFolderRv = findViewById(R.id.mFolderRv);
        mFolderNameLayout = findViewById(R.id.mFolderNameLayout);
        mFolderNameTv = findViewById(R.id.mFolderNameTv);
        mPreviewLayout = findViewById(R.id.mPreviewLayout);
        mPreviewTv = findViewById(R.id.mPreviewTv);
    }

    @Override
    public void onInitListener() {
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mConfirmLayout.setOnClickListener(this);
        mFolderLayout.setOnClickListener(this);
        mFolderNameLayout.setOnClickListener(this);
        mPreviewLayout.setOnClickListener(this);
        mImageRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        mGridAdapter = new ImageGridAdapter(this, mMaxCount, isSingle, isViewImage);
        mImageRv.setAdapter(mGridAdapter);
        mGridAdapter.setOnItemSelectListener(new ImageGridAdapter.OnItemSelectListener() {
            @Override
            public void onOverSelected(int limit) {

            }

            @Override
            public void onSelected(List<ImageItem> mSelectImages) {
                setSelectImageCount(mSelectImages.size());
            }
        });
        mGridAdapter.setOnItemClickListener(new ImageGridAdapter.OnItemClickListener() {
            @Override
            public void onCameraClick() {
                mPresenter.checkPermissionAndCamera();
            }

            @Override
            public void onItemClick(ImageItem imageItem, int position) {
                toPreviewActivity(mGridAdapter.getData(), position);
            }
        });
    }

    @Override
    public void onCheckExternalPermission() {
        mPresenter.checkPermissionAndLoadImages();
    }

    @Override
    public void onLoadFolders(ArrayList<ImageFolder> folders) {
        this.mFolders = folders;
        initFolderList();
        mFolders.get(0).setUseCamera(useCamera);
        setFolder(mFolders.get(0));
        if (mSelectedImages != null && mGridAdapter != null) {
            mGridAdapter.setSelectedImages(mSelectedImages);
            setSelectImageCount(mGridAdapter.getSelectedItems().size());
        }
    }

    /**
     * 改变时间条显示的时间（显示图片列表中的第一个可见图片的时间）
     */
    private void changeTime() {
        if (mImageRv.getLayoutManager() == null) return;
        int firstVisibleItem = ((GridLayoutManager) mImageRv.getLayoutManager()).findFirstVisibleItemPosition();
        ImageItem image = mGridAdapter.getFirstVisibleImage(firstVisibleItem);
        mPresenter.changeTime(mTimeTextView, image);
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
        ImageFolderAdapter mFolderAdapter = new ImageFolderAdapter(this, mFolders);
        mFolderRv.setAdapter(mFolderAdapter);
        mFolderAdapter.setOnFolderSelectListener(new ImageFolderAdapter.OnFolderSelectListener() {
            @Override
            public void OnFolderSelect(ImageFolder folder) {
                setFolder(folder);
            }
        });
    }

    /**
     * 设置选中的文件夹，同时刷新图片列表
     */
    private void setFolder(ImageFolder folder) {
        if (folder != null && mGridAdapter != null && !folder.equals(mFolder)) {
            mFolder = folder;
            mFolderNameTv.setText(folder.getName());
            mImageRv.scrollToPosition(0);
            mGridAdapter.refresh(folder.getImages(), folder.isUseCamera());
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
        if (requestCode == ImagePicker.PREVIEW_RESULT_CODE) {
            if (tempSelectImages != null) {
                mGridAdapter.setSelectedImages(tempSelectImages);
                setSelectImageCount(mGridAdapter.getSelectedItems().size());
                tempSelectImages = null;
            }
            if (data != null && data.getBooleanExtra(ImagePicker.IS_CONFIRM, false)) {
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
        //因为图片的实体类是Image，而我们返回的是String数组，所以要进行转换。
        List<ImageItem> selectImages = mGridAdapter.getSelectedItems();
        ArrayList<String> images = new ArrayList<>();
        for (ImageItem image : selectImages) {
            images.add(image.getPath());
        }
        //点击确定，把选中的图片通过Intent传给上一个Activity。
        mPresenter.setResult(RESULT_OK, images, false);
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
            if (isSingle) {
                textConfirm = getString(R.string.imagePicker_confirm);
            } else if (mMaxCount > 0) {
                textConfirm = textConfirm + "(" + count + "/" + mMaxCount + ")";
            } else {
                textConfirm = textConfirm + "(" + count + ")";
            }
        }
        mConfirmTv.setText(textConfirm);
        mPreviewTv.setText(textPreview);
    }

    private void toPreviewActivity(List<ImageItem> images, int position) {
        if (images != null && !images.isEmpty()) {
            ImagePreviewActivity.openActivity(this, images, mGridAdapter.getSelectedItems(), isSingle, mMaxCount, position);
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
        if (tempSelectImages != null && !tempSelectImages.isEmpty()) {
            tempSelectImages.clear();
        }
        tempSelectImages = null;
        mPresenter.destroy();
        super.onDestroy();
    }
}
