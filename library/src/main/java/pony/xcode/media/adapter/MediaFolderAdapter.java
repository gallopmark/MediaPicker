package pony.xcode.media.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pony.xcode.media.R;
import pony.xcode.media.bean.MediaFolder;
import pony.xcode.media.bean.MediaBean;
import pony.xcode.media.MediaPicker;

import java.util.ArrayList;
import java.util.List;

public class MediaFolderAdapter extends RecyclerView.Adapter<MediaFolderAdapter.MyViewHolder> {
    private Context mContext;
    private List<MediaFolder> mFolders;
    private LayoutInflater mInflater;
    private int mSelectItem;

    private OnFolderSelectListener onFolderSelectListener;

    public MediaFolderAdapter(Context context, List<MediaFolder> folders) {
        this.mContext = context;
        this.mFolders = folders;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.mediaselector_adapter_folder_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final MediaFolder folder = mFolders.get(position);
        ArrayList<MediaBean> images = folder.getItemList();
        holder.tvName.setText(folder.getName());
        holder.ivSelect.setVisibility(mSelectItem == position ? View.VISIBLE : View.GONE);
        String text = images != null && !images.isEmpty() ? images.size() + "张" : "0张";
        if (images != null && !images.isEmpty()) {
            MediaPicker.getDisplacer().displayFolder(mContext, images.get(0), holder.ivImage);
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
        void OnFolderSelect(MediaFolder folder);
    }

    public void setOnFolderSelectListener(OnFolderSelectListener onFolderSelectListener) {
        this.onFolderSelectListener = onFolderSelectListener;
    }
}
