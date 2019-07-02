package com.gallopmark.imagepicker.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gallopmark.imagepicker.R;
import com.gallopmark.imagepicker.bean.ImageFolder;
import com.gallopmark.imagepicker.bean.ImageItem;
import com.gallopmark.imagepicker.loader.DefaultImageLoader;
import com.gallopmark.imagepicker.loader.ImageLoader;
import com.gallopmark.imagepicker.model.ImageSource;

import java.util.ArrayList;
import java.util.List;

public class ImageFolderAdapter extends RecyclerView.Adapter<ImageFolderAdapter.MyViewHolder> {
    private Context mContext;
    private List<ImageFolder> mFolders;
    private LayoutInflater mInflater;
    private int mSelectItem;

    private OnFolderSelectListener onFolderSelectListener;

    public ImageFolderAdapter(Context mContext, List<ImageFolder> mFolders) {
        this.mContext = mContext;
        this.mFolders = mFolders;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_folder_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final ImageFolder folder = mFolders.get(position);
        ArrayList<ImageItem> images = folder.getImages();
        holder.tvName.setText(folder.getName());
        holder.ivSelect.setVisibility(mSelectItem == position ? View.VISIBLE : View.GONE);
        String text = images != null && !images.isEmpty() ? images.size() + "张" : "0张";
        if (images != null && !images.isEmpty()) {
            ImageSource.getDisplacer().displayFolder(mContext, images.get(0).getPath(), holder.ivImage);
//            Glide.with(mContext).load(new File(images.get(0).getPath()))
//                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
//                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(0);
        }
        holder.tvCount.setText(text);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectItem = holder.getAdapterPosition();
                notifyDataSetChanged();
                if (onFolderSelectListener != null) {
                    onFolderSelectListener.OnFolderSelect(folder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFolders == null ? 0 : mFolders.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvCount;
        ImageView ivSelect;
        View vDivider;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvCount = itemView.findViewById(R.id.tvCount);
            ivSelect = itemView.findViewById(R.id.ivSelect);
            vDivider = itemView.findViewById(R.id.vDivider);
        }
    }

    public interface OnFolderSelectListener {
        void OnFolderSelect(ImageFolder folder);
    }

    public void setOnFolderSelectListener(OnFolderSelectListener onFolderSelectListener) {
        this.onFolderSelectListener = onFolderSelectListener;
    }
}
