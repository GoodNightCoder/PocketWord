package com.cyberlight.pocketword.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

import com.cyberlight.pocketword.data.db.entity.Word;

public class CollectWord extends Word {

    @ColumnInfo(name = "import_at", typeAffinity = ColumnInfo.INTEGER)
    private long importAt;

    @ColumnInfo(name = "known", typeAffinity = ColumnInfo.INTEGER)
    private boolean known;

    public CollectWord(long wordId, @NonNull String wordStr, @NonNull String mean, String accent, String audio, long importAt, boolean known) {
        super(wordId, wordStr, mean, accent, audio);
        this.importAt = importAt;
        this.known = known;
    }

    public long getImportAt() {
        return importAt;
    }

    public void setImportAt(long importAt) {
        this.importAt = importAt;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }
}