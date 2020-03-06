package pony.xcode.media.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimationUtil {
    private AnimationUtil() {
    }

    public static void moveToViewBottom(final View v, long duration) {
        if (v.getVisibility() != View.VISIBLE) return;
        v.setEnabled(false);
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        animation.setDuration(duration);
        v.clearAnimation();
        v.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
                v.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 从控件的底部移动到控件所在位置
     *
     * @param duration 动画时间
     */
    public static void bottomMoveToViewLocation(final View v, long duration) {
        if (v.getVisibility() == View.VISIBLE) return;
        v.setEnabled(false);
        v.setVisibility(View.VISIBLE);
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(duration);
        v.clearAnimation();
        v.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 从控件所在位置移动到控件的顶部
     *
     * @param duration 动画时间
     */
    public static void moveToViewTop(final View v, long duration) {
        if (v.getVisibility() != View.VISIBLE) return;
        v.setEnabled(false);
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        animation.setDuration(duration);
        v.clearAnimation();
        v.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
                v.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 从控件的顶部移动到控件所在位置
     *
     * @param duration 动画时间
     */
    public static void topMoveToViewLocation(final View v, long duration) {
        v.setVisibility(View.VISIBLE);
        v.setEnabled(false);
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(duration);
        v.clearAnimation();
        v.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.VISIBLE);
                v.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
