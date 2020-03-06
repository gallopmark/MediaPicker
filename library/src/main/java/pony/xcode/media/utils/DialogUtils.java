package pony.xcode.media.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.gallopmark.imagepicker.R;

public class DialogUtils {
    //相机不可用弹窗提示
    public static void showUnusableCamera(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.imagePicker_dialog_title)
                .setMessage(R.string.imagePicker_open_camera_failure)
                .setPositiveButton(R.string.imagePicker_dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    //不允许或禁止访问存储权限弹窗提示

    /**
     * @param activity          当前activity
     * @param message           提示信息
     * @param listener          再次申请权限回调
     * @param finishWhenCancel  点击取消是否退出页面
     */
    public static void showDismissPermission(final Activity activity, String message, final DialogInterface.OnClickListener listener, final boolean finishWhenCancel) {
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(activity.getString(R.string.imagePicker_dialog_title))
                .setMessage(message)
                .setPositiveButton(activity.getString(R.string.imagePicker_dialog_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (listener != null) {
                            listener.onClick(dialogInterface, i);
                        }
//                        if (rationaleSettings) {
//                            ImagePicker.startAppSettings(activity);
//                        } else {
//                            if (listener != null) {
//                                listener.onClick(dialogInterface, i);
//                            }
//                        }
                    }
                }).setNegativeButton(activity.getString(R.string.imagePicker_dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //取消则退出页面
                dialogInterface.dismiss();
                if (finishWhenCancel) {
                    activity.finish();
                }
            }
        }).show();
    }

    public static void unablePlayVideo(Context context){
        new AlertDialog.Builder(context)
                .setTitle(R.string.imagePicker_dialog_title)
                .setMessage(R.string.imagePicker_play_video_error)
                .setPositiveButton(R.string.imagePicker_dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
