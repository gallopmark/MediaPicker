package pony.xcode.media.loader;

import android.content.Context;


import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import pony.xcode.media.bean.MediaBean;
import pony.xcode.media.utils.SDKVersionUtils;

import java.io.Serializable;

/*默认加载器
 * 使用默认加载器时项目中需要引用Glide图片加载器，否则编译出错
 * */
public class DefaultImageLoader implements ImageLoader, Serializable {
    @Override
    public void displayFolder(@NonNull Context context, @NonNull MediaBean bean, @NonNull ImageView target) {
        Glide.with(context).load(SDKVersionUtils.isAndroidQAbove() ? bean.getUri() : bean.getPath())
                .into(target);
    }

    @Override
    public void displayGrid(@NonNull Context context, @NonNull MediaBean bean, @NonNull ImageView target) {
        Glide.with(context).load(SDKVersionUtils.isAndroidQAbove() ? bean.getUri() : bean.getPath())
                .centerCrop()
                .into(target);
    }

    @Override
    public void displayPager(@NonNull Context context, @NonNull MediaBean bean, @NonNull final ImageView target) {
        Glide.with(context).asBitmap()
                .load(SDKVersionUtils.isAndroidQAbove() ? bean.getUri() : bean.getPath())
                .fitCenter()
                .into(target);
    }
}
