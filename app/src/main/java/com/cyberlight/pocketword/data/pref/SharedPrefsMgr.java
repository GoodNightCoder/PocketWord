package com.cyberlight.pocketword.data.pref;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SharedPrefsMgr implements PrefsMgr {

    private static SharedPrefsMgr sInstance;

    private final SharedPreferences mSharedPreferences;

    private SharedPrefsMgr(SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }

    public static SharedPrefsMgr getInstance(Context context) {
        if (sInstance == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                    context.getApplicationContext());
            sInstance = new SharedPrefsMgr(sharedPreferences);
        }
        return sInstance;
    }

    @Override
    public long getStudyRemindTime() {
        return mSharedPreferences.getLong(PrefsConst.STUDY_REMIND_TIME_KEY, 68400);
    }

    @Override
    public void setStudyRemindTime(long studyRemindTime) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(PrefsConst.STUDY_REMIND_TIME_KEY, studyRemindTime);
        editor.apply();
    }

    @Override
    public boolean isStudyRemind() {
        return mSharedPreferences.getBoolean(PrefsConst.STUDY_REMIND_KEY, false);
    }

    @Override
    public void setStudyRemind(boolean studyRemind) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(PrefsConst.STUDY_REMIND_KEY, studyRemind);
        editor.apply();
    }

    @PrefsConst.OrderType
    @Override
    public int getSortOrder() {
        return mSharedPreferences.getInt(PrefsConst.SORT_ORDER_KEY, PrefsConst.ORDER_BY_ALPHABET_ASC);
    }

    @Override
    public void setSortOrder(@PrefsConst.OrderType int sortOrder) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(PrefsConst.SORT_ORDER_KEY, sortOrder);
        editor.apply();
    }

    @Override
    public long getUsingWordBookId() {
        return mSharedPreferences.getLong(PrefsConst.USING_WORD_BOOK_ID_KEY, 0);
    }

    @Override
    public void setUsingWordBookId(long usingWordBookId) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(PrefsConst.USING_WORD_BOOK_ID_KEY, usingWordBookId);
        editor.apply();
    }
}
