package com.holike.imagepicker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.gallopmark.imagepicker.loader.ImageLoader;
import com.gallopmark.imagepicker.model.ImagePicker;
import com.gallopmark.imagepicker.utils.ImageUtil;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ImagePicker.ImagePickerBuilder()
                .maxSelectCount(20)
//                .imageLoader(new MyImageLoader())
                .create().start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, data.getStringArrayListExtra(ImagePicker.SELECT_RESULT).get(0), Toast.LENGTH_LONG).show();
        }
    }

    private class MyImageLoader implements ImageLoader {

        @Override
        public void displayFolder(@NonNull Context context, @Nullable String path, @NonNull ImageView target) {
            Glide.with(context).load(path).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).into(target);
        }

        @Override
        public void displayGrid(@NonNull Context context, @Nullable String path, @NonNull ImageView target) {
            Glide.with(context).load(path).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).into(target);
        }

        @Override
        public void displayPager(@NonNull Context context, @Nullable String path, @NonNull final ImageView target) {
            Glide.with(context).asBitmap().load(path)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(new ImageViewTarget<Bitmap>(target) {
                        @Override
                        protected void setResource(@Nullable Bitmap resource) {
                            if (resource == null) return;
                            int width = resource.getWidth();
                            int height = resource.getHeight();
                            if (width > 8192 || height > 8192) {
                                Bitmap newBitmap = ImageUtil.zoomBitmap(resource, 8192, 8192);
                                setBitmap(target, newBitmap);
                            } else {
                                setBitmap(target, resource);
                            }
                        }
                    });
        }
    }

    private void setBitmap(ImageView imageView, Bitmap bitmap) {
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

}
