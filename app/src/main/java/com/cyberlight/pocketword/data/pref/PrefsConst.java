package com.cyberlight.pocketword.data.pref;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PrefsConst {
    public static final String STUDY_REMIND_KEY = "study_remind";
    public static final String STUDY_REMIND_TIME_KEY = "study_remind_time";
    public static final String SORT_ORDER_KEY = "sort_order";
    public static final String USING_WORD_BOOK_ID_KEY = "using_word_book_id";
    public static final String MANAGE_STARTUP_APPS_KEY = "manage_startup_apps";
    public static final String IGNORE_BATTERY_OPTIMIZATION_KEY = "ignore_battery_optimization";
    public static final int ORDER_BY_ALPHABET_ASC = 0;
    public static final int ORDER_BY_ALPHABET_DESC = 1;
    public static final int ORDER_BY_IMPORT_TIME_ASC = 2;
    public static final int ORDER_BY_IMPORT_TIME_DESC = 3;


    @IntDef({ORDER_BY_ALPHABET_ASC,
            ORDER_BY_ALPHABET_DESC,
            ORDER_BY_IMPORT_TIME_ASC,
            ORDER_BY_IMPORT_TIME_DESC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrderType {
    }
}
