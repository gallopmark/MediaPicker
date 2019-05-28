package com.gallopmark.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gallopmark.imagepicker.adapter.ImagePageAdapter;
import com.gallopmark.imagepicker.bean.ImageItem;
import com.gallopmark.imagepicker.model.ImagePicker;
import com.gallopmark.imagepicker.presenter.ImagePreviewPresenter;
import com.gallopmark.imagepicker.view.ImagePreviewView;
import com.gallopmark.imagepicker.widget.OnRecyclerViewPageChangedListener;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewActivity extends ImageActivity implements ImagePreviewView {
    private FrameLayout mContainer;
    private Toolbar toolBar;
    private TextView mTitleTv;
    private FrameLayout mConfirmLayout;
    private TextView mConfirmTv;
    private RecyclerView mPagerRecyclerView;
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

    private PagerSnapHelper pagerSnapHelper;

    public static void openActivity(Activity activity, List<ImageItem> images,
                                    List<ImageItem> selectImages, boolean isSingle,
                                    int maxSelectCount, int position) {
        tempImages = images;
        tempSelectImages = selectImages;
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        intent.putExtra(ImagePicker.MAX_SELECT_COUNT, maxSelectCount);
        intent.putExtra(ImagePicker.IS_SINGLE, isSingle);
        intent.putExtra(ImagePicker.POSITION, position);
        activity.startActivityForResult(intent, ImagePicker.PREVIEW_RESULT_CODE);
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
        mPagerRecyclerView = findViewById(R.id.mPagerRecyclerView);
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
        pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mPagerRecyclerView);
        final ImagePageAdapter pageAdapter = new ImagePageAdapter(this, mImages);
        mPagerRecyclerView.setAdapter(pageAdapter);
        pageAdapter.setOnItemClickListener(new ImagePageAdapter.OnItemClickListener() {
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
        mPagerRecyclerView.addOnScrollListener(new OnRecyclerViewPageChangedListener(pagerSnapHelper, new OnRecyclerViewPageChangedListener.OnPageChangeListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }

            @Override
            public void onPageSelected(int position) {
                setTitle((position + 1) + "/" + mImages.size());
                setCheckStatus(position);
            }
        }));
        int initPosition = getIntent().getIntExtra(ImagePicker.POSITION, 0);
        setCheckStatus(initPosition);
        mPagerRecyclerView.scrollToPosition(initPosition);
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
                int currentItem = getCurrentItem();
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
                    if (isSingle) {
                        mSelectImages.clear();
                        mSelectImages.add(imageItem);
                        setCheckItem();
                    } else {
                        if (mSelectImages.size() < mMaxCount) {
                            mSelectImages.add(imageItem);
                            setCheckItem();
                        }
                    }
                }
                setConfirm(mSelectImages.size());
            }
        });
    }

    private int getCurrentItem() {
        RecyclerView.LayoutManager layoutManager = mPagerRecyclerView.getLayoutManager();
        if (layoutManager == null) return -1;
        View view = pagerSnapHelper.findSnapView(layoutManager);
        if (view != null) {
            //获取itemView的position
            return layoutManager.getPosition(view);
        }
        return -1;
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
