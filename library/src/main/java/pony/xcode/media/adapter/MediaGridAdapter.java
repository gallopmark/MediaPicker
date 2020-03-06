package pony.xcode.media.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pony.xcode.media.MediaConfig;
import com.gallopmark.imagepicker.R;
import pony.xcode.media.bean.MediaBean;
import pony.xcode.media.MediaSelector;
import pony.xcode.media.utils.DoubleUtils;

import java.util.ArrayList;
import java.util.List;

public class MediaGridAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private Context mContext;
    private List<MediaBean> mImages;
    private LayoutInflater mInflater;
    private static final int TYPE_CAMERA = 1;
    private static final int TYPE_IMAGE = 2;

    private int mMaxCount;
    private boolean isSingle;
    private boolean isViewImage;
    private boolean isUseCamera;
    private int mChooseMode;

    private OnItemClickListener onItemClickListener;
    private OnItemSelectListener onItemSelectListener;

    private List<MediaBean> mSelectedItems;

    /**
     * @param maxCount    图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     * @param isSingle    是否单选
     * @param isViewImage 是否点击放大图片查看
     */
    public MediaGridAdapter(Context context, int maxCount, boolean isSingle, boolean isViewImage, int chooseMode) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mMaxCount = maxCount;
        this.isSingle = isSingle;
        this.isViewImage = isViewImage;
        this.mChooseMode = chooseMode;
        this.mSelectedItems = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (isUseCamera && position == 0) {
            return TYPE_CAMERA;
        } else {
            return TYPE_IMAGE;
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            View view = mInflater.inflate(R.layout.mediaselector_adapter_grid_item, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = mInflater.inflate(R.layout.mediaselector_adapter_camera_item, parent, false);
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
                    if (!DoubleUtils.isFastDoubleClick() && onItemClickListener != null) {
                        onItemClickListener.onCameraClick();
                    }
                }
            });
        } else {
            final MediaBean mediaBean = getImageItem(position);
            final ImageViewHolder holder = (ImageViewHolder) viewHolder;
            MediaSelector.getDisplacer().displayGrid(mContext, mediaBean.getPath(), holder.mImageView);
//            Glide.with(mContext).load(new File(imageItem.getPath()))
//                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
//                    .into(holder.mImageView);
            if (mChooseMode == MediaConfig.MODE_VIDEO) {
                holder.durationTextView.setText(mediaBean.getFormatDurationTime());
                holder.durationTextView.setVisibility(View.VISIBLE);
                holder.mGifImageView.setVisibility(View.GONE);
            } else {
                holder.durationTextView.setVisibility(View.GONE);
                holder.mGifImageView.setVisibility(mediaBean.isGif() ? View.VISIBLE : View.GONE);
            }
            setItemSelect(holder, mSelectedItems.contains(mediaBean));
            holder.mSelectedIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkedImage(holder, mediaBean);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isViewImage) {
                        if (onItemClickListener != null) {
                            int adapterPosition = holder.getAdapterPosition();
                            onItemClickListener.onItemClick(mediaBean, isUseCamera ? adapterPosition - 1 : adapterPosition);
                        }
                    } else {
                        checkedImage(holder, mediaBean);
                    }
                }
            });
        }
    }

    private void checkedImage(ImageViewHolder holder, MediaBean mediaBean) {
        if (mSelectedItems.contains(mediaBean)) {    //如果图片已经选中，就取消选中
            mSelectedItems.remove(mediaBean);
            setItemSelect(holder, false);
            onItemSelectedChanged();
        } else {
            if (isSingle) {
                clearImageSelect();
                mSelectedItems.add(mediaBean);
                setItemSelect(holder, true);
                onItemSelectedChanged();
            } else {
                if (mMaxCount <= 0 || mSelectedItems.size() < mMaxCount) {
                    mSelectedItems.add(mediaBean);
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
                notifyItemChanged(isUseCamera ? index + 1 : index);
            }
        }
    }

    private void setItemSelect(ImageViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.mSelectedIv.setImageResource(R.drawable.ic_image_selected);
            holder.mMasking.setAlpha(0.5f);
        } else {
            holder.mSelectedIv.setImageResource(R.drawable.ic_image_unselect);
            holder.mMasking.setAlpha(0f);
        }
    }

    private void onItemSelectedChanged() {
        if (onItemSelectListener != null) {
            onItemSelectListener.onSelected(mSelectedItems);
        }
    }

    private MediaBean getImageItem(int position) {
        return mImages.get(isUseCamera ? position - 1 : position);
    }

    @Override
    public int getItemCount() {
        return isUseCamera ? getImageCount() + 1 : getImageCount();
    }

    private int getImageCount() {
        return mImages == null ? 0 : mImages.size();
    }

    public MediaBean getFirstVisibleImage(int firstVisibleItem) {
        if (mImages != null && !mImages.isEmpty()) {
            if (firstVisibleItem >= 0 && firstVisibleItem < mImages.size()) {
                if (isUseCamera) {
                    return mImages.get(firstVisibleItem == 0 ? 0 : firstVisibleItem - 1);
                } else {
                    return mImages.get(firstVisibleItem);
                }
            }
        }
        return null;
    }

    public void refresh(@NonNull ArrayList<MediaBean> data, boolean useCamera) {
        mImages = data;
        this.isUseCamera = useCamera;
        notifyDataSetChanged();
    }

    private boolean isFull() {
        if (isSingle && mSelectedItems.size() == 1) {
            return true;
        } else return mMaxCount > 0 && mSelectedItems.size() == mMaxCount;
    }

    public void setSelectedImages(List<MediaBean> selected) {
        if (mImages != null && selected != null) {
            mSelectedItems.clear();
            for (MediaBean mediaBean : selected) {
                if (isFull()) break;
                if (!mSelectedItems.contains(mediaBean)) {
                    mSelectedItems.add(mediaBean);
                }
            }
            notifyDataSetChanged();
        }
    }


    public List<MediaBean> getSelectedItems() {
        return mSelectedItems;
    }

    public List<MediaBean> getData() {
        return mImages;
    }

    private static class ImageViewHolder extends BaseViewHolder {
        ImageView mImageView;
        View mMasking;
        ImageView mGifImageView;
        ImageView mSelectedIv;
        TextView durationTextView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.mImageView);
            mMasking = itemView.findViewById(R.id.mMasking);
            mGifImageView = itemView.findViewById(R.id.mGifImageView);
            mSelectedIv = itemView.findViewById(R.id.mSelectedImageView);
            durationTextView = itemView.findViewById(R.id.tv_duration);
        }
    }

    private static class CameraViewHolder extends BaseViewHolder {

        CameraViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnItemSelectListener {
        void onSelected(List<MediaBean> mSelectImages);
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    public interface OnItemClickListener {
        void onCameraClick();

        void onItemClick(MediaBean mediaBean, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

}
