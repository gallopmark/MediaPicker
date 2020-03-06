package pony.xcode.media;

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

import com.gallopmark.imagepicker.R;

import pony.xcode.media.adapter.ImagePagerAdapter;
import pony.xcode.media.adapter.MediaBasePagerAdapter;
import pony.xcode.media.adapter.OnPagerItemClickListener;
import pony.xcode.media.adapter.VideoPagerAdapter;
import pony.xcode.media.bean.MediaBean;
import pony.xcode.media.presenter.MediaPreviewPresenter;
import pony.xcode.media.view.MediaPreviewView;

import java.util.ArrayList;
import java.util.List;

/*图片预览页面*/
public class MediaPreviewActivity extends MediaBaseActivity implements MediaPreviewView {
    private FrameLayout mContainer;
    private Toolbar toolBar;
//    private TextView mTitleTv;
    private FrameLayout mConfirmLayout;
    private TextView mConfirmTv;
    private ViewPager mViewPager;
    private LinearLayout llBottom;
    private TextView mSelectTextView;

    private boolean isShowBar = true;

    //tempImages和tempSelectImages用于图片列表数据的页面传输。
    //之所以不要Intent传输这两个图片列表，因为要保证两位页面操作的是同一个列表数据，同时可以避免数据量大时，
    // 用Intent传输发生的错误问题。
    private static List<MediaBean> tempImages;
    private static List<MediaBean> tempSelectImages;

    private List<MediaBean> mImages = new ArrayList<>();
    private List<MediaBean> mSelectImages = new ArrayList<>();

    private int mChooseMode = MediaConfig.MODE_IMAGE;
    private boolean isSingle;
    private int mMaxCount;

    private Drawable mSelectDrawable, mUnSelectDrawable;

    private MediaPreviewPresenter mPresenter;
    private boolean isConfirm = false;

    public static void openActivity(Activity activity, int mode, List<MediaBean> images,
                                    List<MediaBean> selectImages, boolean isSingle,
                                    int maxSelectCount, int position) {
        tempImages = images;
        tempSelectImages = selectImages;
        Intent intent = new Intent(activity, MediaPreviewActivity.class);
        intent.putExtras(dataPackages(mode, images, selectImages, isSingle, maxSelectCount, position));
        activity.startActivityForResult(intent, MediaSelector.PREVIEW_RESULT_CODE);
    }

    private static Bundle dataPackages(int mode, List<MediaBean> images, List<MediaBean> selectImages, boolean isSingle,
                                       int maxSelectCount, int position) {
        tempImages = images;
        tempSelectImages = selectImages;
        Bundle bundle = new Bundle();
        bundle.putInt(MediaSelector.CHOOSE_MODE, mode);
        bundle.putInt(MediaSelector.MAX_SELECT_COUNT, maxSelectCount);
        bundle.putBoolean(MediaSelector.IS_SINGLE, isSingle);
        bundle.putInt(MediaSelector.POSITION, position);
        return bundle;
    }

    @Override
    protected int bindContentView() {
        return R.layout.activity_media_preview;
    }

    @Override
    protected void setup(@Nullable Bundle savedInstanceState) {
        mPresenter = MediaPreviewPresenter.from(this);
        mPresenter.initView();
    }

    @Override
    public void onGetIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mChooseMode = bundle.getInt(MediaSelector.CHOOSE_MODE, MediaConfig.MODE_IMAGE);
            mMaxCount = bundle.getInt(MediaSelector.MAX_SELECT_COUNT, 0);
            isSingle = bundle.getBoolean(MediaSelector.IS_SINGLE, false);
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
//        mTitleTv = findViewById(R.id.mTitleTv);
        mConfirmLayout = findViewById(R.id.mConfirmLayout);
        mConfirmTv = findViewById(R.id.mConfirmTv);
        mViewPager = findViewById(R.id.mViewPager);
        llBottom = findViewById(R.id.llBottom);
        mSelectTextView = findViewById(R.id.mSelectTextView);
        mSelectDrawable = ContextCompat.getDrawable(this, R.drawable.ic_image_selected);
        mUnSelectDrawable = ContextCompat.getDrawable(this, R.drawable.ic_image_unselect);
    }

    @Override
    public void initViewData() {
        setTitle("1/" + mImages.size());
        setConfirm(mSelectImages.size());
    }

    private void setTitle(String title) {
        toolBar.setTitle(title);
//        mTitleTv.setText(title);
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
//        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(this, mImages);
        mViewPager.clearOnPageChangeListeners();
        mViewPager.setOffscreenPageLimit(1);
        int initPosition = getIntent().getIntExtra(MediaSelector.POSITION, 0);
        final MediaBasePagerAdapter pagerAdapter;
        if (mChooseMode == MediaConfig.MODE_VIDEO) {
            pagerAdapter = new VideoPagerAdapter(this, mImages, mViewPager, initPosition);
        } else {
            pagerAdapter = new ImagePagerAdapter(this, mImages);
        }
        pagerAdapter.setOnPagerItemClickListener(new OnPagerItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (isShowBar) {
                    mPresenter.hideBar(mContainer, toolBar, llBottom);
                    isShowBar = false;
                } else {
                    mPresenter.showBar(mContainer, toolBar, llBottom);
                    isShowBar = true;
                }
            }
        });
        mViewPager.setAdapter(pagerAdapter);
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
                MediaBean mediaBean = mImages.get(currentItem);
                if (mSelectImages.contains(mediaBean)) {
                    if (isSingle) {
                        mSelectImages.clear();
                    } else {
                        mSelectImages.remove(mediaBean);
                    }
                    setUnCheckItem();
                } else {
                    /*单选情况下 清空已选图片再添加选中图片*/
                    if (isSingle) {
                        mSelectImages.clear();
                        mSelectImages.add(mediaBean);
                        setCheckItem();
                    } else {
                        /*未指定maxCount 或者选择的数量没有超过maxCount*/
                        if (mMaxCount == 0 || mSelectImages.size() < mMaxCount) {
                            mSelectImages.add(mediaBean);
                            setCheckItem();
                        }
                    }
                }
                setConfirm(mSelectImages.size());
            }
        });
    }

    private void setCheckStatus(int position) {
        if (position < 0 || position > mImages.size()) return;
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
    protected void onResume() {
        super.onResume();
        if(mViewPager.getAdapter() instanceof VideoPagerAdapter){
            VideoPagerAdapter adapter = (VideoPagerAdapter) mViewPager.getAdapter();
            adapter.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mViewPager.getAdapter() instanceof VideoPagerAdapter){
            VideoPagerAdapter adapter = (VideoPagerAdapter) mViewPager.getAdapter();
            adapter.pause();
        }
    }

    @Override
    protected void onDestroy() {
        if(mViewPager.getAdapter() instanceof VideoPagerAdapter){
            VideoPagerAdapter adapter = (VideoPagerAdapter) mViewPager.getAdapter();
            adapter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        MediaPickerActivity.onPreviewResult(this, mSelectImages, isConfirm);
        super.finish();
    }
}
