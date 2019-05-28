package com.gallopmark.imagepicker.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.github.chrisbanes.photoview.PhotoView;
import com.gallopmark.imagepicker.R;
import com.gallopmark.imagepicker.bean.ImageItem;
import com.gallopmark.imagepicker.utils.ImageUtil;

import java.util.List;

public class ImagePageAdapter extends RecyclerView.Adapter<ImagePageAdapter.PageHolder> {
    private Context context;
    private List<ImageItem> images;
    private LayoutInflater mInflater;
    private OnItemClickListener mListener;

    public ImagePageAdapter(Context context, List<ImageItem> images) {
        this.context = context;
        this.images = images;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new PageHolder(mInflater.inflate(R.layout.adapter_photoview, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final PageHolder holder, int position) {
        Glide.with(context).asBitmap().load(images.get(position).getPath())
                .into(new ImageViewTarget<Bitmap>(holder.photoView) {
                    @Override
                    protected void setResource(@Nullable Bitmap resource) {
                        if (resource == null) return;
                        int width = resource.getWidth();
                        int height = resource.getHeight();
                        if (width > 8192 || height > 8192) {
                            Bitmap newBitmap = ImageUtil.zoomBitmap(resource, 8192, 8192);
                            setBitmap(holder.photoView, newBitmap);
                        } else {
                            setBitmap(holder.photoView, resource);
                        }
                    }
                });
        holder.photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onItemClick(holder.getAdapterPosition(), images.get(holder.getAdapterPosition()));
            }
        });
    }

    private void setBitmap(PhotoView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        if (bitmap != null) {
            int bw = bitmap.getWidth();
            int bh = bitmap.getHeight();
            int vw = imageView.getWidth();
            int vh = imageView.getHeight();
            if (bw != 0 && bh != 0 && vw != 0 && vh != 0) {
                if (1.0f * bh / bw > 1.0f * vh / vw) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }

    static class PageHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        PageHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, ImageItem imageItem);
    }
}
