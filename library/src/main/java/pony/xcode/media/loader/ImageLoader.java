package pony.xcode.media.loader;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ImageView;


public interface ImageLoader{
    /*加载图片文件夹列表第一张图
    *   Glide.with(mContext).load(new File(path)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(holder.ivImage);
    * */
    void displayFolder(@NonNull Context context, @Nullable String path, @NonNull ImageView target);

    /**
     * 加载相册图片
     * Glide.with(mContext).load(new File(imageItem.getPath()))
     * .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
     * .into(holder.mImageView);
     */
    void displayGrid(@NonNull Context context, @Nullable String path, @NonNull ImageView target);

    /**
     * 加载大图方法
     * Glide.with(context).asBitmap().load(images.get(position).getPath())
     * .into(new ImageViewTarget<Bitmap>(holder.photoView) {
     *
     * protected void setResource(@Nullable Bitmap resource) {
     * if (resource == null) return;
     * int width = resource.getWidth();
     * int height = resource.getHeight();
     * if (width > 8192 || height > 8192) {
     * Bitmap newBitmap = ImageUtil.zoomBitmap(resource, 8192, 8192);
     * setBitmap(holder.photoView, newBitmap);
     * } else {
     * setBitmap(holder.photoView, resource);
     * }
     * }
     * });
     * <p>
     * private void setBitmap(PhotoView imageView, Bitmap bitmap) {
     * imageView.setImageBitmap(bitmap);
     * if (bitmap != null) {
     * int bw = bitmap.getWidth();
     * int bh = bitmap.getHeight();
     * int vw = imageView.getWidth();
     * int vh = imageView.getHeight();
     * if (bw != 0 && bh != 0 && vw != 0 && vh != 0) {
     * if (1.0f * bh / bw > 1.0f * vh / vw) {
     * imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
     * } else {
     * imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
     * }
     * }
     * }
     * }
     */

    void displayPager(@NonNull Context context, @Nullable String path, @NonNull ImageView target);
}
