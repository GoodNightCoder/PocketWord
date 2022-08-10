package com.cyberlight.pocketword.data.pref;

public interface PrefsMgr {
    long getStudyRemindTime();

    void setStudyRemindTime(long studyRemindTime);

    boolean isStudyRemind();

    void setStudyRemind(boolean studyRemind);

    int getSortOrder();

    void setSortOrder(int sortOrder);

    long getUsingWordBookId();

    void setUsingWordBookId(long usingWordBookId);
}