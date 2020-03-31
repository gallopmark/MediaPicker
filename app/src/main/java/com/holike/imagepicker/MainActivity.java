package com.holike.imagepicker;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import pony.xcode.media.bean.MediaBean;
import pony.xcode.media.MediaPicker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.iv_image);
        Glide.get(getApplicationContext()).clearMemory();
        MediaPicker.builder().maxSelectCount(6)
//                .sizeLimit(5 * 1024 * 1024)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            ArrayList<MediaBean> images = data.getParcelableArrayListExtra(MediaPicker.SELECT_RESULT);
            Glide.with(this).load(images.get(0).getPath()).into(imageView);
        }
    }
}
