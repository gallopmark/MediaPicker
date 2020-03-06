package pony.xcode.media.utils;

import android.content.Context;

import com.gallopmark.imagepicker.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String getImageTime(Context context, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Calendar imageTime = Calendar.getInstance();
        imageTime.setTimeInMillis(time);
        if (sameDay(calendar, imageTime)) {
            return context.getString(R.string.imagePicker_time_today);
        } else if (sameWeek(calendar, imageTime)) {
            return context.getString(R.string.imagePicker_time_currentWeek);
        } else if (sameMonth(calendar, imageTime)) {
            return context.getString(R.string.imagePicker_time_currentMonth);
        } else {
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM", Locale.getDefault());
            return sdf.format(date);
        }
    }

    private static boolean sameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean sameWeek(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR);
    }

    private static boolean sameMonth(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }

    /**
     * 毫秒转时分秒
     * @param time 毫秒数
     */
    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                : String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
