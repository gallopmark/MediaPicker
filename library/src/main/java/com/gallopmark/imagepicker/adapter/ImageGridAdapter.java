package com.gallopmark.imagepicker.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.gallopmark.imagepicker.R;
import com.gallopmark.imagepicker.bean.ImageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageGridAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private Context mContext;
    private List<ImageItem> mImages;
    private LayoutInflater mInflater;
    private static final int TYPE_CAMERA = 1;
    private static final int TYPE_IMAGE = 2;

    private int mMaxCount;
    private boolean isSingle;
    private boolean isViewImage;
    private boolean useCamera;

    private OnItemClickListener onItemClickListener;
    private OnItemSelectListener onItemSelectListener;

    private List<ImageItem> mSelectedItems;

    /**
     * @param maxCount    图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     * @param isSingle    是否单选
     * @param isViewImage 是否点击放大图片查看
     */
    public ImageGridAdapter(Context context, int maxCount, boolean isSingle, boolean isViewImage) {
        mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        mMaxCount = maxCount;
        this.isSingle = isSingle;
        this.isViewImage = isViewImage;
        mSelectedItems = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (useCamera && position == 0) {
            return TYPE_CAMERA;
        } else {
            return TYPE_IMAGE;
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            View view = mInflater.inflate(R.layout.adapter_image_item, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = mInflater.inflate(R.layout.adapter_camera_item, parent, false);
            return new CameraViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int position) {
        int itemType = viewHolder.getItemViewType();
        if (itemType == TYPE_CAMERA) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onCameraClick();
                    }
                }
            });
        } else {
            final ImageItem imageItem = getImageItem(position);
            final ImageViewHolder holder = (ImageViewHolder) viewHolder;
            Glide.with(mContext).load(new File(imageItem.getPath()))
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(holder.mImageView);
            holder.mGifImageView.setVisibility(imageItem.isGif() ? View.VISIBLE : View.GONE);
            setItemSelect(holder, mSelectedItems.contains(imageItem));
            holder.mSelectedIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkedImage(holder, imageItem);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isViewImage) {
                        if (onItemClickListener != null) {
                            int adapterPosition = holder.getAdapterPosition();
                            onItemClickListener.onItemClick(imageItem, useCamera ? adapterPosition - 1 : adapterPosition);
                        }
                    } else {
                        checkedImage(holder, imageItem);
                    }
                }
            });
        }
    }

    private void checkedImage(ImageViewHolder holder, ImageItem imageItem) {
        if (mSelectedItems.contains(imageItem)) {    //如果图片已经选中，就取消选中
            mSelectedItems.remove(imageItem);
            setItemSelect(holder, false);
            onItemSelectedChanged();
        } else {
            if (isSingle) {
                clearImageSelect();
                mSelectedItems.add(imageItem);
                setItemSelect(holder, true);
                onItemSelectedChanged();
            } else {
                if (mMaxCount <= 0 || mSelectedItems.size() < mMaxCount) {
                    mSelectedItems.add(imageItem);
                    setItemSelect(holder, true);
                    onItemSelectedChanged();
                }
            }
        }
    }

    private void clearImageSelect() {
        if (mImages != null && mSelectedItems.size() == 1) {
            int index = mImages.indexOf(mSelectedItems.get(0));
            mSelectedItems.clear();
            if (index != -1) {
                notifyItemChanged(useCamera ? index + 1 : index);
            }
        }
    }

    private void setItemSelect(ImageViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.mSelectedIv.setImageResource(R.drawable.icon_image_select);
            holder.mMasking.setAlpha(0.5f);
        } else {
            holder.mSelectedIv.setImageResource(R.drawable.icon_image_un_select);
            holder.mMasking.setAlpha(0f);
        }
    }

    private void onItemSelectedChanged() {
        if (onItemSelectListener != null) {
            onItemSelectListener.onSelected(mSelectedItems);
        }
    }

    private ImageItem getImageItem(int position) {
        return mImages.get(useCamera ? position - 1 : position);
    }

    @Override
    public int getItemCount() {
        return useCamera ? getImageCount() + 1 : getImageCount();
    }

    private int getImageCount() {
        return mImages == null ? 0 : mImages.size();
    }

    public ImageItem getFirstVisibleImage(int firstVisibleItem) {
        if (mImages != null && !mImages.isEmpty()) {
            if (useCamera) {
                return mImages.get(firstVisibleItem == 0 ? 0 : firstVisibleItem - 1);
            } else {
                return mImages.get(firstVisibleItem);
            }
        }
        return null;
    }

    public void refresh(@NonNull ArrayList<ImageItem> data, boolean useCamera) {
        mImages = data;
        this.useCamera = useCamera;
        notifyDataSetChanged();
    }

    private boolean isFull() {
        if (isSingle && mSelectedItems.size() == 1) {
            return true;
        } else return mMaxCount > 0 && mSelectedItems.size() == mMaxCount;
    }

    public void setSelectedImages(List<ImageItem> selected) {
        if (mImages != null && selected != null) {
            mSelectedItems.clear();
            for (ImageItem imageItem : selected) {
                if (isFull()) break;
                if (!mSelectedItems.contains(imageItem)) {
                    mSelectedItems.add(imageItem);
                }
            }
            notifyDataSetChanged();
        }
    }


    public List<ImageItem> getSelectedItems() {
        return mSelectedItems;
    }

    public List<ImageItem> getData() {
        return mImages;
    }

    private static class ImageViewHolder extends BaseViewHolder {
        ImageView mImageView;
        View mMasking;
        ImageView mGifImageView;
        ImageView mSelectedIv;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.mImageView);
            mMasking = itemView.findViewById(R.id.mMasking);
            mGifImageView = itemView.findViewById(R.id.mGifImageView);
            mSelectedIv = itemView.findViewById(R.id.mSelectedImageView);
        }
    }

    private static class CameraViewHolder extends BaseViewHolder {

        CameraViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnItemSelectListener {
        void onSelected(List<ImageItem> mSelectImages);
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    public interface OnItemClickListener {
        void onCameraClick();

        void onItemClick(ImageItem imageItem, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

}
