package com.cyberlight.pocketword.data.pref;

public interface PrefsMgr {
    long getStudyRemindTime();

    void setStudyRemindTime(long studyRemindTime);

    boolean isStudyRemind();

    void setStudyRemind(boolean studyRemind);

    long getUsingWordBookId();

    void setUsingWordBookId(long usingWordBookId);

    boolean isSkipKnown();

    void setSkipKnown(boolean skipKnown);

    int getDailyGoal();

    void setDailyGoal(int dailyGoal);
}