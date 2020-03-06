package pony.xcode.media.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.TextViewCompat;


import java.lang.reflect.Field;

import pony.xcode.media.R;


public class MediaTitleView extends Toolbar {
    private TextView mTitleTextView;  //title textView
    private CharSequence mTitleText;    //title text
    private int mTitleTextAppearance;

    public MediaTitleView(Context context) {
        this(context, null);
    }

    public MediaTitleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaTitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttribute(context, attrs);
    }

    private void resolveAttribute(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MediaTitleView);
        final int titleTextAppearance = ta.getResourceId(R.styleable.MediaTitleView_mtv_titleAppearance, 0);
        setTitleTextAppearance(titleTextAppearance);
        final CharSequence titleText = ta.getText(R.styleable.MediaTitleView_mtv_titleText);
        if (!TextUtils.isEmpty(titleText)) {
            setTitle(titleText);
        }
        ta.recycle();
    }

    @Override
    public CharSequence getTitle() {
        return mTitleText;
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            if (mTitleTextView == null) {
                mTitleTextView = new TextView(getContext());
                mTitleTextView.setSingleLine();
                mTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
//                mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleTextSize);
                if (obtainTitleTextAppearance() != 0) {
                    TextViewCompat.setTextAppearance(mTitleTextView, obtainTitleTextAppearance());
                }
            }
            if (mTitleTextView.getParent() != this) {
                addTitleCenter(mTitleTextView);
            }
        } else if (mTitleTextView != null && mTitleTextView.getParent() == this) {// 当title为空时，remove
            removeView(mTitleTextView);
        }
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
        mTitleText = title;
    }

    private void addTitleCenter(View v) {
        ViewGroup.LayoutParams vlp = v.getLayoutParams();
        Toolbar.LayoutParams lp;
        if (vlp == null) {
            lp = generateDefaultLayoutParams();
        } else if (!checkLayoutParams(vlp)) {
            lp = generateLayoutParams(vlp);
        } else {
            lp = (Toolbar.LayoutParams) vlp;
        }
        lp.gravity = Gravity.CENTER;
        lp.setMargins(getTitleMarginStart(), getTitleMarginTop(), getTitleMarginEnd(), getTitleMarginBottom());
        addView(v, lp);
    }

    @Override
    public void setTitleTextColor(int titleTextColor) {
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(titleTextColor);
        }
    }

    public void setTitleTextAppearance(int titleTextAppearance) {
        if (mTitleTextView != null) {
            TextViewCompat.setTextAppearance(mTitleTextView, titleTextAppearance);
        }
        this.mTitleTextAppearance = titleTextAppearance;
    }

    public int obtainTitleTextAppearance() {
        return mTitleTextAppearance;
    }

    @Override
    public void setTitleTextAppearance(Context context, int resId) {
        if (mTitleTextView != null) {
            TextViewCompat.setTextAppearance(mTitleTextView, resId);
        }
    }

    @Override
    public void setNavigationIcon(@Nullable Drawable icon) {
        super.setNavigationIcon(icon);
        setGravityCenter();
    }

    private void setGravityCenter() {
        setCenter("mNavButtonView");
        setCenter("mMenuView");
    }

    private void setCenter(String fieldName) {
        Object obj = getField(fieldName);//拿到对应的Object
        if (obj == null) return;
        if (obj instanceof View) {
            ViewGroup.LayoutParams lp = ((View) obj).getLayoutParams();//拿到LayoutParams
            if (lp instanceof ActionBar.LayoutParams) {
                ((ActionBar.LayoutParams) lp).gravity = Gravity.CENTER;//设置居中
                ((View) obj).setLayoutParams(lp);
            }
        }
    }

    /*通过反射获取属性值*/
    @Nullable
    protected Object getField(@NonNull String fieldName) {
        try {
            if (getClass().getSuperclass() == null) return null;
            Field field = getClass().getSuperclass().getDeclaredField(fieldName);//反射得到父类Field
            field.setAccessible(true);
            return field.get(this);
        } catch (Exception e) {
            return null;
        }
    }
}
