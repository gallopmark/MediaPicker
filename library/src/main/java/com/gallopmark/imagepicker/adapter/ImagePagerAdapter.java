package com.gallopmark.imagepicker.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gallopmark.imagepicker.R;
import com.gallopmark.imagepicker.bean.ImageItem;
import com.gallopmark.imagepicker.loader.DefaultImageLoader;
import com.gallopmark.imagepicker.loader.ImageLoader;
import com.gallopmark.imagepicker.model.ImageSource;
import com.gallopmark.imagepicker.widget.PinchImageView;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {
    private Context context;
    private List<ImageItem> images;
    private LayoutInflater mInflater;
    private OnItemClickListener mListener;

    public ImagePagerAdapter(Context context, List<ImageItem> images) {
        this.context = context;
        this.images = images;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return images == null ? 0 : images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View view = mInflater.inflate(R.layout.adapter_photoview, container, false);
        PinchImageView photoView = view.findViewById(R.id.photoView);
        ImageSource.getInstance().getDisplacer().displayPager(context, images.get(position).getPath(), photoView);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(position, images.get(position));
                }
            }
        });
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, ImageItem imageItem);
    }
}
