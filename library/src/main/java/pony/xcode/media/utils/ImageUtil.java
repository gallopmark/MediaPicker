package pony.xcode.media.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;


import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtil {

    public static String saveImage(Bitmap bitmap, String path) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String filename = dateFormat.format(new Date(System.currentTimeMillis())) + ".png";
//        String name = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".png";
        FileOutputStream b = null;
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            return "";
        }
        String fileName = path + File.separator + filename;
        try {
            b = new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, b);// 把数据写入文件
            return fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (b != null) {
                    b.flush();
                    b.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static Bitmap zoomBitmap(Bitmap bm, int reqWidth, int reqHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) reqWidth) / width;
        float scaleHeight = ((float) reqHeight) / height;
        float scale = Math.min(scaleWidth, scaleHeight);
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    public static Bitmap getBitmapFromUri(Context c, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = c.getContentResolver()
                    .openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                return image;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据计算的inSampleSize，得到压缩后图片
     */
    public static Bitmap decodeSampledBitmapFromFile(Context context, String pathName, boolean isUriPath, int reqWidth, int reqHeight) {
        if (isUriPath) {
            return getBitmapFromUri(context, Uri.parse(pathName));
        } else {
            try {
                // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(pathName, options);
                // 调用上面定义的方法计算inSampleSize值
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                // 使用获取到的inSampleSize值再次解析图片
                options.inJustDecodeBounds = false;
//            options.inPreferredConfig = Bitmap.Config.RGB_565;
                return BitmapFactory.decodeFile(pathName, options);
            } catch (OutOfMemoryError error) {
                Log.e("eee", "内存泄露！");
                return null;
            }
        }
    }

    /**
     * 计算inSampleSize，用于压缩图片
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (width > reqWidth && height > reqHeight) {
//         计算出实际宽度和目标宽度的比率
            int widthRatio = Math.round((float) width / (float) reqWidth);
            int heightRatio = Math.round((float) height / (float) reqHeight);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }

        return inSampleSize;
    }

}