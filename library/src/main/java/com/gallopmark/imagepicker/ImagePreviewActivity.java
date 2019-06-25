package com.gallopmark.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gallopmark.imagepicker.adapter.ImagePagerAdapter;
import com.gallopmark.imagepicker.bean.ImageItem;
import com.gallopmark.imagepicker.model.ImagePicker;
import com.gallopmark.imagepicker.presenter.ImagePreviewPresenter;
import com.gallopmark.imagepicker.view.ImagePreviewView;

import java.util.ArrayList;
import java.util.List;

/*图片预览页面*/
public class ImagePreviewActivity extends BaseActivity implements ImagePreviewView {
    private FrameLayout mContainer;
    private Toolbar toolBar;
    private TextView mTitleTv;
    private FrameLayout mConfirmLayout;
    private TextView mConfirmTv;
    private ViewPager mViewPager;
    private LinearLayout llBottom;
    private TextView mSelectTextView;

    private boolean isShowBar = true;

    //tempImages和tempSelectImages用于图片列表数据的页面传输。
    //之所以不要Intent传输这两个图片列表，因为要保证两位页面操作的是同一个列表数据，同时可以避免数据量大时，
    // 用Intent传输发生的错误问题。
    private static List<ImageItem> tempImages;
    private static List<ImageItem> tempSelectImages;

    private List<ImageItem> mImages = new ArrayList<>();
    private List<ImageItem> mSelectImages = new ArrayList<>();

    private boolean isSingle;
    private int mMaxCount;

    private Drawable mSelectDrawable, mUnSelectDrawable;

    private ImagePreviewPresenter mPresenter;
    private boolean isConfirm = false;

    public static void openActivity(Activity activity, List<ImageItem> images,
                                    List<ImageItem> selectImages, boolean isSingle,
                                    int maxSelectCount, int position) {
        tempImages = images;
        tempSelectImages = selectImages;
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        intent.putExtras(dataPackages(images, selectImages, isSingle, maxSelectCount, position));
        activity.startActivityForResult(intent, ImagePicker.PREVIEW_RESULT_CODE);
    }

    private static Bundle dataPackages(List<ImageItem> images, List<ImageItem> selectImages, boolean isSingle,
                                       int maxSelectCount, int position) {
        tempImages = images;
        tempSelectImages = selectImages;
        Bundle bundle = new Bundle();
        bundle.putInt(ImagePicker.MAX_SELECT_COUNT, maxSelectCount);
        bundle.putBoolean(ImagePicker.IS_SINGLE, isSingle);
        bundle.putInt(ImagePicker.POSITION, position);
        return bundle;
    }

    @Override
    protected int bindContentView() {
        return R.layout.activity_image_preview;
    }

    @Override
    protected void setup(@Nullable Bundle savedInstanceState) {
        mPresenter = ImagePreviewPresenter.from(this);
        mPresenter.initView();
    }

    @Override
    public void onGetIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mMaxCount = bundle.getInt(ImagePicker.MAX_SELECT_COUNT, 0);
            isSingle = bundle.getBoolean(ImagePicker.IS_SINGLE, false);
        }
        if (tempImages != null) {
            mImages.addAll(tempImages);
            tempImages = null;
        }
        if (tempSelectImages != null) {
            mSelectImages.addAll(tempSelectImages);
            tempSelectImages = null;
        }
    }

    @Override
    public void onInitView() {
        mContainer = findViewById(R.id.mContainer);
        toolBar = findViewById(R.id.toolBar);
        mTitleTv = findViewById(R.id.mTitleTv);
        mConfirmLayout = findViewById(R.id.mConfirmLayout);
        mConfirmTv = findViewById(R.id.mConfirmTv);
        mViewPager = findViewById(R.id.mViewPager);
        llBottom = findViewById(R.id.llBottom);
        mSelectTextView = findViewById(R.id.mSelectTextView);
        mSelectDrawable = ContextCompat.getDrawable(this, R.drawable.icon_image_select);
        mUnSelectDrawable = ContextCompat.getDrawable(this, R.drawable.icon_image_un_select);
    }

    @Override
    public void initViewData() {
        setTitle("1/" + mImages.size());
        setConfirm(mSelectImages.size());
    }

    private void setTitle(String title) {
        mTitleTv.setText(title);
    }

    private void setConfirm(int count) {
        String textConfirm = getString(R.string.imagePicker_confirm);
        if (count == 0) {
            mConfirmLayout.setEnabled(false);
        } else {
            mConfirmLayout.setEnabled(true);
            textConfirm = textConfirm + "(" + count + ")";
        }
        mConfirmTv.setText(textConfirm);
    }

    @Override
    public void initViewPager() {
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(this, mImages);
        mViewPager.setAdapter(pagerAdapter);
        pagerAdapter.setOnItemClickListener(new ImagePagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, ImageItem imageItem) {
                if (isShowBar) {
                    mPresenter.hideBar(mContainer, toolBar, llBottom);
                    isShowBar = false;
                } else {
                    mPresenter.showBar(mContainer, toolBar, llBottom);
                    isShowBar = true;
                }
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                setTitle((position + 1) + "/" + mImages.size());
                setCheckStatus(position);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        int initPosition = getIntent().getIntExtra(ImagePicker.POSITION, 0);
        setCheckStatus(initPosition);
        mViewPager.setCurrentItem(initPosition);
    }

    @Override
    public void onInitListener() {
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mConfirmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isConfirm = true;
                finish();
            }
        });
        mSelectTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItem = mViewPager.getCurrentItem();
                if (currentItem < 0 || currentItem > mImages.size()) return;
                ImageItem imageItem = mImages.get(currentItem);
                if (mSelectImages.contains(imageItem)) {
                    if (isSingle) {
                        mSelectImages.clear();
                    } else {
                        mSelectImages.remove(imageItem);
                    }
                    setUnCheckItem();
                } else {
                    /*单选情况下 清空已选图片再添加选中图片*/
                    if (isSingle) {
                        mSelectImages.clear();
                        mSelectImages.add(imageItem);
                        setCheckItem();
                    } else {
                        /*未指定maxCount 或者选择的数量没有超过maxCount*/
                        if (mMaxCount == 0 || mSelectImages.size() < mMaxCount) {
                            mSelectImages.add(imageItem);
                            setCheckItem();
                        }
                    }
                }
                setConfirm(mSelectImages.size());
            }
        });
    }

    private void setCheckStatus(int position) {
        int index = mSelectImages.indexOf(mImages.get(position));
        if (index >= 0 && index < mSelectImages.size()) {
            setCheckItem();
        } else {
            setUnCheckItem();
        }
    }

    private void setCheckItem() {
        mSelectTextView.setCompoundDrawablesWithIntrinsicBounds(mSelectDrawable, null, null, null);
    }

    private void setUnCheckItem() {
        mSelectTextView.setCompoundDrawablesWithIntrinsicBounds(mUnSelectDrawable, null, null, null);
    }

    @Override
    public void finish() {
        ImagePickerActivity.onPreviewResult(this, mSelectImages, isConfirm);
        super.finish();
    }
}
