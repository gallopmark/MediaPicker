package pony.xcode.media.adapter;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class MediaBasePagerAdapter<T> extends PagerAdapter {
    Context mContext;
    List<T> mDataList;
    OnPagerItemClickListener mListener;

    MediaBasePagerAdapter(Context context, List<T> dataList) {
        this.mContext = context;
        this.mDataList = dataList;
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public void setOnPagerItemClickListener(OnPagerItemClickListener listener) {
        this.mListener = listener;
    }
}
