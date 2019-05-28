package com.gallopmark.imagepicker;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;

public abstract class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(bindContentView());
        setupToolbar();
        setup(savedInstanceState);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        if (toolbar != null) {
            Drawable navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_back);
            if (navigationIcon != null)
                navigationIcon.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
            toolbar.setNavigationIcon(navigationIcon);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    protected abstract int bindContentView();

    protected abstract void setup(@Nullable Bundle savedInstanceState);
}
