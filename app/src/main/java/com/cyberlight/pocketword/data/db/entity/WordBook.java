package com.cyberlight.pocketword.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_wordbook")
public class WordBook {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "word_book_id", typeAffinity = ColumnInfo.INTEGER)
    private long wordBookId;

    @NonNull
    @ColumnInfo(name = "word_book_name", typeAffinity = ColumnInfo.TEXT)
    private String wordBookName;

    @ColumnInfo(name = "learning_progress", typeAffinity = ColumnInfo.INTEGER, defaultValue = "0")
    private int learningProgress;

    public WordBook(long wordBookId, @NonNull String wordBookName, int learningProgress) {
        this.wordBookId = wordBookId;
        this.wordBookName = wordBookName;
        this.learningProgress = learningProgress;
    }

    // 新建词书时使用该构造方法
    @Ignore
    public WordBook(@NonNull String wordBookName) {
        this.wordBookName = wordBookName;
    }

    public long getWordBookId() {
        return wordBookId;
    }

    public void setWordBookId(long wordBookId) {
        this.wordBookId = wordBookId;
    }

    @NonNull
    public String getWordBookName() {
        return wordBookName;
    }

    public void setWordBookName(@NonNull String wordBookName) {
        this.wordBookName = wordBookName;
    }

    public int getLearningProgress() {
        return learningProgress;
    }

    public void setLearningProgress(int learningProgress) {
        this.learningProgress = learningProgress;
    }
}
