package pony.xcode.media;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;


abstract class MediaBaseActivity extends AppCompatActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.ImagePickerTheme);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(bindContentView());
        setupToolbar();
        setup(savedInstanceState);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        int titleHeight = getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        if (MediaPicker.getTitleHeight() > 0) {
            titleHeight = MediaPicker.getTitleHeight();
        }
        ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
        lp.height = titleHeight;
        toolbar.setLayoutParams(lp);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    protected abstract int bindContentView();

    protected abstract void setup(@Nullable Bundle savedInstanceState);
}
