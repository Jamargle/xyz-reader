package com.example.xyzreader.utils;

import android.text.format.DateUtils;
import android.util.Log;

import com.example.xyzreader.ui.articles.ArticleListActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public final class DateParsingUtil {

    private static final String TAG = ArticleListActivity.class.toString();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.getDefault());
    private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private static final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private DateParsingUtil() {
    }

    public static String getFormattedPublishedDateBeforeStartOfEpoch(final String publishedDate) {
        final Date date = parsePublishedDate(publishedDate);
        return DateUtils.getRelativeTimeSpanString(
                date.getTime(),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString();
    }

    public static String getFormattedPublishedDateAfterStartOfEpoch(final String publishedDate) {
        final Date date = parsePublishedDate(publishedDate);
        return OUTPUT_FORMAT.format(date);
    }

    public static Date parsePublishedDate(final String stringPublishedDate) {
        try {
            return DATE_FORMAT.parse(stringPublishedDate);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    public static boolean isPreviousStartOfEpoch(final Date date) {
        return !date.before(START_OF_EPOCH.getTime());
    }

}
