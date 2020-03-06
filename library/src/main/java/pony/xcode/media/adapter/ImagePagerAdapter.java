package pony.xcode.media.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gallopmark.imagepicker.R;
import pony.xcode.media.bean.MediaBean;
import pony.xcode.media.MediaSelector;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

//图片预览适配器
public class ImagePagerAdapter extends MediaBasePagerAdapter<MediaBean> {
    private LayoutInflater mInflater;

    public ImagePagerAdapter(Context context, List<MediaBean> dataList) {
        super(context, dataList);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View view = mInflater.inflate(R.layout.adapter_photoview, container, false);
        PhotoView photoView = view.findViewById(R.id.photoView);
        MediaSelector.getDisplacer().displayPager(mContext, mDataList.get(position).getPath(), photoView);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(position);
                }
            }
        });
        container.addView(view);
        return view;
    }
}
