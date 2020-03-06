package pony.xcode.media.utils;

import android.os.Build;

public class SDKVersionUtils {

    //是否是android Q 版本以上
    public static boolean isAndroidQAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    //是否是android 4.4版本以上
    static boolean isKitkatAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
